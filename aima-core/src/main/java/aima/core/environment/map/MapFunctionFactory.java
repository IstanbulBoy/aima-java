package aima.core.environment.map;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import aima.core.agent.Action;
import aima.core.agent.Percept;
import aima.core.agent.impl.DynamicPercept;
import aima.core.search.framework.problem.ActionsFunction;
import aima.core.search.framework.problem.ResultFunction;
import aima.core.util.math.geom.shapes.Point2D;

/**
 * @author Ciaran O'Reilly
 * @author Ruediger Lunde
 * 
 */
public class MapFunctionFactory {
	private static ResultFunction resultFunction;
	private static Function<Percept, Object> perceptToStateFunction;

	public static ActionsFunction getActionsFunction(Map map) {
		return new MapActionsFunction(map, false);
	}
	
	public static ActionsFunction getReverseActionsFunction(Map map) {
		return new MapActionsFunction(map, true);
	}

	public static ResultFunction getResultFunction() {
		if (null == resultFunction) {
			resultFunction = new MapResultFunction();
		}
		return resultFunction;
	}


	/** Returns a heuristic function based on straight line distance computation. */
	public static Function<Object, Double> getSLDHeuristicFunction(Object goal, Map map) {
		return new StraightLineDistanceHeuristicFunction(goal, map);
	}


	private static class MapActionsFunction implements ActionsFunction {
		private Map map = null;
		private boolean reverseMode;

		public MapActionsFunction(Map map, boolean reverseMode) {
			this.map = map;
			this.reverseMode = reverseMode;
		}

		public Set<Action> actions(Object state) {
			Set<Action> actions = new LinkedHashSet<>();
			String location = state.toString();

			List<String> linkedLocations = reverseMode ? map.getPossiblePrevLocations(location)
					: map.getPossibleNextLocations(location);
			for (String linkLoc : linkedLocations) {
				actions.add(new MoveToAction(linkLoc));
			}
			return actions;
		}
	}


	public static Function<Percept, Object> getPerceptToStateFunction() {
		if (null == perceptToStateFunction) {
			perceptToStateFunction = p -> ((DynamicPercept) p).getAttribute(DynAttributeNames.PERCEPT_IN);
		}
		return perceptToStateFunction;
	}

	private static class MapResultFunction implements ResultFunction {
		public MapResultFunction() {
		}

		public Object result(Object s, Action a) {
			if (a instanceof MoveToAction) {
				MoveToAction mta = (MoveToAction) a;
				return mta.getToLocation();
			}
			// The Action is not understood or is a NoOp
			// the result will be the current state.
			return s;
		}
	}

	private static class StraightLineDistanceHeuristicFunction implements Function<Object, Double> {
		private Object goal;
		private Map map;

		public StraightLineDistanceHeuristicFunction(Object goal, Map map) {
			this.goal = goal;
			this.map = map;
		}

		@Override
		public Double apply(Object state) {
			double result = 0.0;
			Point2D pt1 = map.getPosition((String) state);
			Point2D pt2 = map.getPosition((String) goal);
			if (pt1 != null && pt2 != null) {
				result = pt1.distance(pt2);
			}
			return result;
		}
	}
}
