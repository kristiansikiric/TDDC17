public class StateAndReward {

	static int MAXVX = 5;
	static int MAXVY = 5;
	static int numberOfStates = 10;
	/* State discretization function for the angle controller */
	public static String getStateAngle(double angle, double vx, double vy) {
		
		String state = "OneStateToRuleThemAll";
		
		/*if(angle == 0 || angle == -0){
			state = "GoalState";
		}
		else if(angle > 0.0 && angle <= Math.PI/2){
			state = "UpRight";
		}
		else if(angle > Math.PI/2 && angle <= Math.PI){
			state = "DownRight";
		}
		else if(angle < 0.0 && angle >= -Math.PI/2){
			state = "UpLeft";
		}
		else if(angle < -Math.PI/2 && angle >= -Math.PI){
			state = "DownLeftw";
		}*/
		state = Integer.toString(discretize(angle, numberOfStates+2, -Math.PI,Math.PI));
				
		
		return state;
	}

	/* Reward function for the angle controller */
	public static double getRewardAngle(double angle, double vx, double vy) {
					
		double reward = 0;
		
		reward = Math.PI - Math.pow(Math.abs(angle),2);

		return reward;
	}

	/* State discretization function for the full hover controller */
	public static String getStateHover(double angle, double vx, double vy) {

		/* TODO: IMPLEMENT THIS FUNCTION */

		String state = "OneStateToRuleThemAll";
		String anglestate = "OneStateToRuleThemAll2";
		String vystate = "OneStateToRuleThemAll2";
		String vxstate = "OneStateToRuleThemAll2";
		
		anglestate = getStateAngle(angle, vx ,vy);
		
		vystate = Integer.toString(discretize(vy, 8, -MAXVY,MAXVY));
		vxstate = Integer.toString(discretize(vx, 8, -MAXVX ,MAXVX));
		
		state = anglestate + "," + vystate + "," + vxstate + ",";
		
		return state;
	}

	/* Reward function for the full hover controller */
	public static double getRewardHover(double angle, double vx, double vy) {

		/* TODO: IMPLEMENT THIS FUNCTION */
		
		double reward = 0;
		
		reward = getRewardAngle(angle,vx,vy)/Math.PI + Math.max(0,MAXVX - Math.abs(vx))/MAXVX + Math.max(0,MAXVY - Math.abs(vy))/MAXVY;
		if (reward < 0){
			return 0;
		}
		return reward;
	}

	// ///////////////////////////////////////////////////////////
	// discretize() performs a uniform discretization of the
	// value parameter.
	// It returns an integer between 0 and nrValues-1.
	// The min and max parameters are used to specify the interval
	// for the discretization.
	// If the value is lower than min, 0 is returned
	// If the value is higher than min, nrValues-1 is returned
	// otherwise a value between 1 and nrValues-2 is returned.
	//
	// Use discretize2() if you want a discretization method that does
	// not handle values lower than min and higher than max.
	// ///////////////////////////////////////////////////////////
	public static int discretize(double value, int nrValues, double min,
			double max) {
		if (nrValues < 2) {
			return 0;
		}

		double diff = max - min;

		if (value < min) {
			return 0;
		}
		if (value > max) {
			return nrValues - 1;
		}

		double tempValue = value - min;
		double ratio = tempValue / diff;

		return (int) (ratio * (nrValues - 2)) + 1;
	}

	// ///////////////////////////////////////////////////////////
	// discretize2() performs a uniform discretization of the
	// value parameter.
	// It returns an integer between 0 and nrValues-1.
	// The min and max parameters are used to specify the interval
	// for the discretization.
	// If the value is lower than min, 0 is returned
	// If the value is higher than min, nrValues-1 is returned
	// otherwise a value between 0 and nrValues-1 is returned.
	// ///////////////////////////////////////////////////////////
	public static int discretize2(double value, int nrValues, double min,
			double max) {
		double diff = max - min;

		if (value < min) {
			return 0;
		}
		if (value > max) {
			return nrValues - 1;
		}

		double tempValue = value - min;
		double ratio = tempValue / diff;

		return (int) (ratio * nrValues);
	}

}
