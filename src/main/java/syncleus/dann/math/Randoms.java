package syncleus.dann.math;

import java.util.Random;

/**
 * Useful class for obtaining various random numbers and also true with given
 * probability.
 *
 * @see Rand#successWithPercent(double)
 * @author Elser
 *
 *         TODO merge with dANN math functions
 */
public class Randoms {
	private static final int arrPercentSize = 97397;
	private static final double arrPercent[];
	static Random random = null;
	static {
		random = new Random();
		arrPercent = new double[arrPercentSize];
		setArrPercent();
	}

	/**
	 * Returns random double in range (from,to)
	 *
	 * @param from
	 *            to
	 */
	public static double d(final double from, final double to) {
		if (to < from)
			return 0;
		return (to - from) * (random.nextDouble()) + from;
	}

	private static void setArrPercent() {
		for (int i = 0; i < arrPercent.length; i++) {
			arrPercent[i] = Randoms.d(100);
		}
	}

	/**
	 * Returns random integer
	 */
	public static int i(final int range) {
		return random.nextInt(range);
	}

	/**
	 * Returns random int in range (from,to)
	 */
	public static int i(final int from, final int to) {
		if (to < from)
			return 0;
		return (Math.abs(random.nextInt()) % (to - from)) + from;
	}

	/**
	 * Returns random boolean
	 */
	public static boolean b() {
		return random.nextBoolean();
	}

	/**
	 * Returns random double in range (0,max)
	 */
	public static double d(final double max) {
		return random.nextDouble() * max;
	}

	private static int iPerc = 0;
	private static int kPerc = 0;

	public static double gauss() {
		final double nextGaussian = random.nextGaussian();
		return nextGaussian;
	}

	public static int i() {
		return random.nextInt();
	}

	public static boolean successWithPercent(final double percent) {
		iPerc++;
		if (iPerc >= arrPercent.length) {
			kPerc++;
			iPerc = 0;
			if (kPerc > 20) {
				setArrPercent();
				kPerc = 0;
			}
		}
		return arrPercent[iPerc] < percent;
	}

	public static double gauss(final double scale) {
		return random.nextGaussian() * scale;
	}

	public static double gaussAbs(final double scale, final double offset) {
		return Math.abs(gauss(scale) + offset);
	}

	/**
	 * Simply returns a random object from given array.
	 */
	public static Object pickRandom(final Object[] arr) {
		return arr[Randoms.i(arr.length)];
	}

	/**
	 * Returns the index of random element from the given array with the
	 * probability proportional to the value of the element
	 */
	public static int pickBestIndex(final double[] arr) {
		double sum = 0.0;
		for (int i = 0; i < arr.length; i++) {
			final double evaluate = arr[i];
			if (evaluate < 0) {
				new Exception("arr[i].evaluate() < 0").printStackTrace();
			}
			sum += evaluate;
		}
		final double randomPick = Randoms.d(sum);
		sum = 0.0;
		for (int i = 0; i < arr.length; i++) {
			sum += arr[i];
			if (randomPick <= sum) {
				return i;
			}
		}
		return Randoms.i(arr.length);
	}
}
