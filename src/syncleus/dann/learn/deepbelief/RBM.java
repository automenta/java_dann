package syncleus.dann.learn.deepbelief;

import java.util.Random;
import syncleus.dann.math.Sigmoids;

public class RBM {

    public final int N;
    public final int n_visible;
    public final int n_hidden;
    public final double[][] W;
    public final double[] hbias;
    public final double[] vbias;
    public final Random rng;

    final double[] ph_mean;
    final double[] ph_sample;
    final double[] nv_means;
    final double[] nv_samples;
    final double[] nh_means;
    final double[] nh_samples;
    
    public double uniform(final double min, final double max) {
        return rng.nextDouble() * (max - min) + min;
    }

    public int binomial1(final double p) {
        return rng.nextDouble() < p ? 1 : 0;
    }
    
    public int binomial(final int n, final double p) {
        /*if (p < 0 || p > 1) {
            return 0;
        }*/
        
        if (n == 1) {
            //fast version, no loop
            return binomial1(p);
        }

        int c = 0;        
        for (int i = 0; i < n; i++) {            
            if (rng.nextDouble() < p)
                c++;
        }

        return c;
    }

    public static double sigmoid(final double x) {       
        //return 1.0 / (1.0 + Math.pow(Math.E, -x));
        //return 1.0 / (1.0 + Math.exp(-x));
        
        return Sigmoids.sigmoidUniFast(x);
    }

    public RBM(int N, int n_visible, int n_hidden,
            double[][] W, double[] hbias, double[] vbias, Random rng) {
        this.N = N;
        this.n_visible = n_visible;
        this.n_hidden = n_hidden;

        ph_mean = new double[n_hidden];
        ph_sample = new double[n_hidden];
        nv_means = new double[n_visible];
        nv_samples = new double[n_visible];
        nh_means = new double[n_hidden];
        nh_samples = new double[n_hidden];
        
        if (rng == null) {
            this.rng = new Random(1234);
        } else {
            this.rng = rng;
        }

        if (W == null) {
            this.W = new double[this.n_hidden][this.n_visible];
            double a = 1.0 / this.n_visible;

            for (int i = 0; i < this.n_hidden; i++) {
                for (int j = 0; j < this.n_visible; j++) {
                    this.W[i][j] = uniform(-a, a);
                }
            }
        } else {
            this.W = W;
        }

        if (hbias == null) {
            this.hbias = new double[this.n_hidden];
            for (int i = 0; i < this.n_hidden; i++) {
                this.hbias[i] = 0;
            }
        } else {
            this.hbias = hbias;
        }

        if (vbias == null) {
            this.vbias = new double[this.n_visible];
            for (int i = 0; i < this.n_visible; i++) {
                this.vbias[i] = 0;
            }
        } else {
            this.vbias = vbias;
        }
    }

    public void contrastive_divergence(final double[] input, final double lr, final int k) {

        /* CD-k */
        sample_h_given_v(input, ph_mean, ph_sample);

        for (int step = 0; step < k; step++) {
            gibbs_hvh(step == 0 ? ph_sample : nh_samples, nv_means, nv_samples, nh_means, nh_samples);
        }

        for (int i = 0; i < n_hidden; i++) {
            final double ph_meanI = ph_mean[i];
            final double nh_meansI = nh_means[i];
            for (int j = 0; j < n_visible; j++) {
                W[i][j] += lr * (ph_meanI * input[j] - nh_meansI * nv_samples[j]) / N;
            }
            hbias[i] += lr * (ph_sample[i] - nh_meansI) / N;
        }

        for (int i = 0; i < n_visible; i++) {
            vbias[i] += lr * (input[i] - nv_samples[i]) / N;
        }

    }

    public void sample_h_given_v(final double[] v0_sample, final double[] mean, final double[] sample) {
        for (int i = 0; i < n_hidden; i++) {
            mean[i] = propup(v0_sample, W[i], hbias[i]);
            sample[i] = binomial1(mean[i]);
        }
    }

    public void sample_v_given_h(final double[] h0_sample, final double[] mean, final double[] sample) {
        for (int i = 0; i < n_visible; i++) {
            mean[i] = propdown(h0_sample, i, vbias[i]);
            sample[i] = binomial1(mean[i]);
        }
    }

    public double propup(final double[] v, final double[] w, final double b) {
        double pre_sigmoid_activation = 0.0;
        for (int j = 0; j < n_visible; j++) {
            pre_sigmoid_activation += w[j] * v[j];
        }
        pre_sigmoid_activation += b;
        return sigmoid(pre_sigmoid_activation);
    }

    public double propdown(final double[] h, final int i, final double b) {
        double pre_sigmoid_activation = 0.0;
        for (int j = 0; j < n_hidden; j++) {
            pre_sigmoid_activation += W[j][i] * h[j];
        }
        pre_sigmoid_activation += b;
        return sigmoid(pre_sigmoid_activation);
    }

    public void gibbs_hvh(double[] h0_sample, double[] nv_means, double[] nv_samples, double[] nh_means, double[] nh_samples) {
        sample_v_given_h(h0_sample, nv_means, nv_samples);
        sample_h_given_v(nv_samples, nh_means, nh_samples);
    }

    public void reconstruct(double[] v, double[] reconstructed_v) {
        double[] h = new double[n_hidden];
        double pre_sigmoid_activation;

        for (int i = 0; i < n_hidden; i++) {
            h[i] = propup(v, W[i], hbias[i]);
        }

        for (int i = 0; i < n_visible; i++) {
            pre_sigmoid_activation = 0.0;
            for (int j = 0; j < n_hidden; j++) {
                pre_sigmoid_activation += W[j][i] * h[j];
            }
            pre_sigmoid_activation += vbias[i];

            reconstructed_v[i] = sigmoid(pre_sigmoid_activation);
        }
    }
}
