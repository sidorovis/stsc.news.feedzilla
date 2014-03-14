package stsc.statistic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import stsc.common.Day;
import stsc.statistic.EquityCurve.EquityCurveElement;
import stsc.statistic.Statistics.StatisticsInit;
import stsc.trading.TradingLog;
import stsc.trading.TradingRecord;

public class StatisticsProcessor {

	public final static double EPSILON = 0.000001;
	private final static double PERCENTS = 100.0;

	private class Positions {
		private class Position {
			private int shares = 0;
			private double spentMoney = 0.0;

			public Position(int shares, double spentMoney) {
				super();
				this.shares = shares;
				this.spentMoney = spentMoney;
			}

			public void increment(int shares, double spentMoney) {
				this.shares += shares;
				this.spentMoney += spentMoney;
			}

			public boolean decrement(int shares, double spentMoney) {
				this.shares -= shares;
				this.spentMoney -= spentMoney;
				return this.shares == 0.0;
			}

			public double sharePrice() {
				return spentMoney / shares;
			}
		}

		private HashMap<String, Position> positions = new HashMap<>();

		void increment(String stockName, int shares, double sharesPrice) {
			Position position = positions.get(stockName);
			if (position != null)
				position.increment(shares, sharesPrice);
			else
				positions.put(stockName, new Position(shares, sharesPrice));
		}

		public void decrement(String stockName, int shares, double sharesPrice) {
			Position position = positions.get(stockName);
			if (position.decrement(shares, sharesPrice))
				positions.remove(stockName);
		}

		public double sharePrice(String stockName) {
			Position position = positions.get(stockName);
			return position.sharePrice();
		}

		public double cost(HashMap<String, Double> prices) {
			double result = 0.0;
			for (Map.Entry<String, Position> i : positions.entrySet()) {
				double price = prices.get(i.getKey());
				result += price * i.getValue().shares;
			}
			return result;
		}
	}

	public static boolean isDoubleEqual(double l, double r) {
		return (Math.abs(l - r) < EPSILON);
	}

	private class EquityProcessor {

		private Date lastDate;
		private HashMap<String, Double> lastPrice = new HashMap<>();
		private ArrayList<TradingRecord> tradingRecords;
		private int tradingRecordsIndex = 0;

		private double spentLongCash = 0;
		private double spentShortCash = 0;

		private Positions longPositions = new Positions();
		private Positions shortPositions = new Positions();

		private double maximumSpentMoney = 0.0;
		private double sumOfStartMonths = 0.0;

		private ArrayList<Double> elementsInStartMonths = new ArrayList<>();
		private ArrayList<Integer> startMonthsIndexes = new ArrayList<>();

		StatisticsInit statisticsInit = Statistics.getInit();

		public EquityProcessor(TradingLog tradingLog) {
			this.tradingRecords = tradingLog.getRecords();
		}

		public void setStockDay(String stockName, Day stockDay) {
			lastDate = stockDay.date;
			lastPrice.put(stockName, stockDay.getPrices().getOpen());
		}

		public void processEod() {
			int tradingRecordSize = tradingRecords.size();
			for (int i = tradingRecordsIndex; i < tradingRecordSize; ++i) {
				TradingRecord record = tradingRecords.get(i);
				String stockName = record.getStockName();

				double price = lastPrice.get(stockName);
				int shares = record.getAmount();
				double sharesPrice = shares * price;

				if (record.isPurchase()) {
					if (record.isLong()) {
						spentLongCash += sharesPrice;
						longPositions.increment(stockName, shares, sharesPrice);
					} else {
						spentShortCash += sharesPrice;
						shortPositions.increment(stockName, shares, sharesPrice);
					}
				} else {
					if (record.isLong()) {
						double oldPrice = longPositions.sharePrice(stockName);
						double priceDiff = shares * (price - oldPrice);
						addPositionClose(priceDiff);
						spentLongCash -= sharesPrice;
						longPositions.decrement(stockName, shares, sharesPrice);
					} else {
						double oldPrice = shortPositions.sharePrice(stockName);
						double priceDiff = shares * (oldPrice - price);
						addPositionClose(priceDiff);
						spentShortCash -= (sharesPrice + 2 * priceDiff);
						shortPositions.decrement(stockName, shares, sharesPrice);
					}
				}
			}
			tradingRecordsIndex = tradingRecordSize;
			double dayCache = spentLongCash + spentShortCash;
			if (maximumSpentMoney < dayCache)
				maximumSpentMoney = dayCache;
			double moneyInLongs = longPositions.cost(lastPrice);
			double moneyInShorts = shortPositions.cost(lastPrice);

			statisticsInit.equityCurve.add(lastDate, dayCache - moneyInLongs - moneyInShorts);
		}

		private void addPositionClose(double moneyDiff) {
			if (moneyDiff >= 0)
				addWin(moneyDiff);
			else
				addLoss(moneyDiff);
		}

		private void addWin(double moneyDiff) {
			statisticsInit.count += 1;
			statisticsInit.winCount += 1;
			statisticsInit.winSum += moneyDiff;
			if (moneyDiff > statisticsInit.maxWin)
				statisticsInit.maxWin = moneyDiff;
		}

		private void addLoss(double moneyDiff) {
			statisticsInit.count += 1;
			statisticsInit.lossCount += 1;
			statisticsInit.lossSum += moneyDiff;
			if (moneyDiff < statisticsInit.maxLoss)
				statisticsInit.maxLoss = moneyDiff;
		}

		public Statistics calculate() throws StatisticsCalculationException {
			maximumSpentMoney /= PERCENTS;
			if (isDoubleEqual(maximumSpentMoney, 0.0))
				return null;
			statisticsInit.equityCurve.recalculateWithMax(maximumSpentMoney);

			calculateEquityStatistics();

			return new Statistics(statisticsInit);
		}

		private void calculateEquityStatistics() {
			final int DAYS_PER_YEAR = 250;
			statisticsInit.period = statisticsInit.equityCurve.size();

			if (statisticsInit.period > DAYS_PER_YEAR) {
				calculateMonthsStatistics();
				collectElementsInStartMonths();
				calculateStartMonthsStatistics();
				calculate12MonthsStatistics();
			}

			calculateDrawDownStatistics();

		}

		private void calculateDrawDownStatistics() {
			final StatisticsInit init = statisticsInit;
			final int equityCurveSize = init.equityCurve.size();

			EquityCurveElement ddStart = init.equityCurve.get(0);
			boolean inDrawdown = false;
			double ddSize = 0.0;
			double lastValue = ddStart.value;
			
			int ddCount = 0;
			double ddDurationSum = 0.0;
			double ddValueSum = 0.0;

			for (int i = 1; i < equityCurveSize; ++i) {
				EquityCurveElement currentElement = init.equityCurve.get(i);
				if (!inDrawdown) {
					if (currentElement.value >= lastValue)
						ddStart = currentElement;
					else {
						inDrawdown = true;
						ddSize = ddStart.value - currentElement.value;
					}
				} else {
					if (currentElement.value > lastValue) {
						if (currentElement.value >= ddStart.value) {
							final int ddLength = Days.daysBetween( new LocalDate( ddStart.date ), new LocalDate( currentElement.date )).getDays();
							
							ddCount +=1 ;
							ddDurationSum += ddLength;
							ddValueSum += ddSize;
							
							checkDdLengthSizeOnMax( ddSize, ddLength );

							inDrawdown = false;
							ddStart = currentElement;
							ddSize = 0.0;
						}
					} else {
						final double currentDdSize = ddStart.value - currentElement.value;
						if (ddSize < currentDdSize)
							ddSize = currentDdSize;
					}
				}
				lastValue = currentElement.value;
			}
			if (inDrawdown){
				final int ddLength = Days.daysBetween( new LocalDate( ddStart.date ), new LocalDate( init.equityCurve.getLastElement().date )).getDays();
				ddCount += 1;
				ddValueSum += ddSize;
				ddDurationSum += ddLength;
				
				checkDdLengthSizeOnMax( ddSize, ddLength );
			}
			
			init.ddDurationAvGain = ddDurationSum / ddCount;
			init.ddValueAvGain = ddValueSum / ddCount;
			
		}

		private void checkDdLengthSizeOnMax(double ddSize, int ddLength) {
			if (ddSize > statisticsInit.ddValueMax )
				statisticsInit.ddValueMax = ddSize;
			if (ddLength > statisticsInit.ddDurationMax )
				statisticsInit.ddDurationMax = ddLength;
		}

		private void collectElementsInStartMonths() {
			final StatisticsInit init = statisticsInit;

			LocalDate nextMonthBegin = new LocalDate(init.equityCurve.get(0).date).plusMonths(1).withDayOfMonth(1);
			final int firstMonthIndex = init.equityCurve.find(nextMonthBegin.toDate());

			final int REASONABLE_AMOUNT_OF_DAYS = 15;
			if (firstMonthIndex >= REASONABLE_AMOUNT_OF_DAYS) {
				startMonthsIndexes.add(0);
			}

			final LocalDate endDate = new LocalDate(init.equityCurve.getLastElement().date);

			int nextIndex = init.equityCurve.size();
			while (nextMonthBegin.isBefore(endDate)) {
				nextIndex = init.equityCurve.find(nextMonthBegin.toDate());
				startMonthsIndexes.add(nextIndex);
				nextMonthBegin = nextMonthBegin.plusMonths(1);
			}
			if (init.equityCurve.size() - nextIndex >= REASONABLE_AMOUNT_OF_DAYS) {
				startMonthsIndexes.add(init.equityCurve.size() - 1);
			}
		}

		private void calculate12MonthsStatistics() {
			final StatisticsInit init = statisticsInit;
			final int MONTHS_PER_YEAR = 12;
			final int startMonthsIndexesSize = startMonthsIndexes.size() - MONTHS_PER_YEAR;

			ArrayList<Double> rollingWindow12Month = new ArrayList<>();
			double rollingWindow12MonthSum = 0.0;

			for (int i = 0; i < startMonthsIndexesSize; ++i) {
				final double beginPeriodValue = init.equityCurve.get(startMonthsIndexes.get(i)).value;
				final double endPeriodValue = init.equityCurve.get(startMonthsIndexes.get(i + MONTHS_PER_YEAR)).value;
				final double diff = endPeriodValue - beginPeriodValue;
				rollingWindow12Month.add(diff);
				rollingWindow12MonthSum += diff;
				if (diff > init.month12Max)
					init.month12Max = diff;
				if (diff < init.month12Min)
					init.month12Min = diff;
			}
			init.month12AvGain = rollingWindow12MonthSum / rollingWindow12Month.size();
			init.month12StdDevGain = calculateStdDev(rollingWindow12MonthSum, rollingWindow12Month);
		}

		private void calculateStartMonthsStatistics() {
			final StatisticsInit init = statisticsInit;
			final int startMonthsIndexesSize = startMonthsIndexes.size();

			double lastValue = init.equityCurve.get(0).value;
			for (int i = 1; i < startMonthsIndexesSize; ++i) {
				double nextValue = init.equityCurve.get(startMonthsIndexes.get(i)).value;
				double differentForMonth = nextValue - lastValue;
				processMonthInStartMonths(differentForMonth);
				lastValue = nextValue;
			}
			init.startMonthAvGain = sumOfStartMonths / elementsInStartMonths.size();
			init.startMonthStdDevGain = calculateStdDev(sumOfStartMonths, elementsInStartMonths);
		}

		private void processMonthInStartMonths(double moneyDiff) {
			elementsInStartMonths.add(moneyDiff);
			sumOfStartMonths += moneyDiff;
			if (moneyDiff > statisticsInit.startMonthMax)
				statisticsInit.startMonthMax = moneyDiff;
			if (moneyDiff < statisticsInit.startMonthMin)
				statisticsInit.startMonthMin = moneyDiff;
		}

		private void calculateMonthsStatistics() {
			final StatisticsInit init = statisticsInit;

			int index = 0;

			LocalDate indexDate = new LocalDate(init.equityCurve.get(index).date);
			LocalDate monthAgo = indexDate.plusMonths(1);

			double indexValue = init.equityCurve.get(index).value;

			double monthsCapitalsSum = 0.0;
			final ArrayList<Double> monthsDifferents = new ArrayList<>();

			final LocalDate endDate = new LocalDate(init.equityCurve.getLastElement().date);

			while (monthAgo.isBefore(endDate)) {
				index = init.equityCurve.find(monthAgo.toDate()) - 1;
				EquityCurveElement element = init.equityCurve.get(index);

				double lastValue = element.value;
				double differentForMonth = lastValue - indexValue;

				monthsDifferents.add(differentForMonth);
				monthsCapitalsSum += differentForMonth;

				indexValue = lastValue;
				monthAgo = monthAgo.plusMonths(1);
			}

			final int REASONABLE_AMOUNT_OF_DAYS = 13;
			if (init.equityCurve.size() - index >= REASONABLE_AMOUNT_OF_DAYS) {
				double lastValue = init.equityCurve.getLastElement().value;
				double differentForMonth = lastValue - indexValue;

				monthsDifferents.add(differentForMonth);
				monthsCapitalsSum += differentForMonth;
			}

			final double RISK_PERCENTS = 5.0;
			final double MONTHS_PER_YEAR = 12.0;
			final double sharpeAnnualReturn = (MONTHS_PER_YEAR / monthsDifferents.size()) * monthsCapitalsSum;
			final double sharpeStdDev = Math.sqrt(MONTHS_PER_YEAR)
					* calculateStdDev(monthsCapitalsSum, monthsDifferents);

			init.sharpeRatio = (sharpeAnnualReturn - RISK_PERCENTS) / sharpeStdDev;
		}
	};

	private EquityProcessor equityProcessor;

	public StatisticsProcessor(TradingLog tradingLog) {
		this.equityProcessor = new EquityProcessor(tradingLog);
	}

	public void setStockDay(String stockName, Day stockDay) {
		equityProcessor.setStockDay(stockName, stockDay);
	}

	public void processEod() {
		equityProcessor.processEod();
	}

	public Statistics calculate() throws StatisticsCalculationException {
		Statistics statisticsData = equityProcessor.calculate();
		equityProcessor = null;
		return statisticsData;
	}

	public double calculateStdDev(List<Double> elements) {
		double summ = 0.0;
		for (Double i : elements) {
			summ += i;
		}
		return calculateStdDev(summ, elements);
	}

	public double calculateStdDev(double summ, List<Double> elements) {
		double result = 0.0;
		int size = elements.size();
		double average = summ / size;
		for (Double i : elements) {
			result += Math.pow((average - i), 2);
		}
		result = Math.sqrt(result / size);
		return result;
	}

}