package stsc.general.simulator.multistarter.genetic;

import java.util.Iterator;
import java.util.Random;

import stsc.algorithms.AlgorithmSettingsImpl;
import stsc.common.FromToPeriod;
import stsc.common.algorithms.AlgorithmSettings;
import stsc.general.simulator.multistarter.AlgorithmParameters;
import stsc.general.simulator.multistarter.MpIterator;
import stsc.general.simulator.multistarter.Parameter;
import stsc.general.simulator.multistarter.ParameterList;
import stsc.general.simulator.multistarter.ParameterType;

public class AlgorithmSettingsGeneticList {

	private final FromToPeriod period;

	private final AlgorithmParameters parameters;
	final Random random = new Random();

	public AlgorithmSettingsGeneticList(final FromToPeriod period, final AlgorithmParameters parameters) {
		this.period = period;
		this.parameters = new AlgorithmParameters(parameters);
	}

	@Override
	public String toString() {
		return period.toString() + parameters.toString();
	}

	public AlgorithmSettings generateRandom() {
		final AlgorithmSettingsImpl algoSettings = new AlgorithmSettingsImpl(period);

		for (int i = 0; i < ParameterType.typesSize; ++i) {
			final ParameterList<?> list = parameters.getParamsFor(i);
			for (MpIterator<?> p : list.getParams()) {
				final String name = p.getName();
				final String value = p.getRangom().toString();
				algoSettings.set(name, value);
			}
		}
		final ParameterList<String> list = parameters.getSubExecutions();
		for (MpIterator<?> p : list.getParams()) {
			final String subExecutionName = p.getRangom().toString();
			algoSettings.addSubExecutionName(subExecutionName);
		}
		return algoSettings;
	}

	public void mutate(final AlgorithmSettings settings) {
		final int parametersAmount = parameters.parametersSize();
		if (parametersAmount > 0) {
			mutateParameter(settings, parametersAmount);
		}
	}

	private void mutateParameter(final AlgorithmSettings settings, final int parametersAmount) {
		int indexOfMutatingParameter = random.nextInt(parametersAmount);

		for (int i = 0; i < ParameterType.typesSize; ++i) {
			final ParameterList<?> list = parameters.getParamsFor(i);
			final int size = list.getParams().size();
			if (size != 0 && size > indexOfMutatingParameter) {
				final MpIterator<?> parameter = list.getParams().get(indexOfMutatingParameter);
				final int sizeOfValues = (int) parameter.size();
				final int mutatedIndex = random.nextInt(sizeOfValues);
				final Parameter<?> p = parameter.getParameterByIndex(mutatedIndex);
				settings.mutate(p.getName(), p.getStringValue());
				return; // this should return from function to avoid mutating of
						// subExecutions
			} else {
				indexOfMutatingParameter -= size;
			}
		}

		final ParameterList<String> list = parameters.getSubExecutions();
		final int size = list.getParams().size();
		if (size != 0 && size > indexOfMutatingParameter) {
			final MpIterator<?> parameter = list.getParams().get(indexOfMutatingParameter);
			final int sizeOfValues = (int) parameter.size();
			final int mutatedIndex = random.nextInt(sizeOfValues);
			final Parameter<?> p = parameter.getParameterByIndex(mutatedIndex);
			settings.mutateSubExecution(indexOfMutatingParameter, p.getStringValue());
		}
	}

	public AlgorithmSettings merge(AlgorithmSettings leftSe, AlgorithmSettings rightSe) {
		return mergeParameters(leftSe, rightSe);
	}

	private AlgorithmSettings mergeParameters(AlgorithmSettings leftSe, AlgorithmSettings rightSe) {
		final AlgorithmSettingsImpl result = new AlgorithmSettingsImpl(period);

		for (int i = 0; i < ParameterType.typesSize; ++i) {
			for (MpIterator<?> p : parameters.getParamsFor(i).getParams()) {
				final String settingName = p.getName();
				final String leftValue = leftSe.get(settingName);
				final String rightValue = rightSe.get(settingName);
				final String mutatedValue = p.mutate(leftValue, rightValue).toString();
				result.set(settingName, mutatedValue);
			}
		}
		final Iterator<MpIterator<String>> subExecutionIterator = parameters.getSubExecutionIterator();
		final Iterator<String> lv = leftSe.getSubExecutions().iterator();
		final Iterator<String> rv = rightSe.getSubExecutions().iterator();
		while (subExecutionIterator.hasNext() && lv.hasNext() && rv.hasNext()) {
			final MpIterator<?> p = subExecutionIterator.next();
			final String leftValue = lv.next();
			final String rightValue = rv.next();
			final String mutatedValue = p.mutate(leftValue, rightValue).toString();
			result.addSubExecutionName(mutatedValue);
		}
		return result;
	}

	public long size() {
		return parameters.size();
	}

}
