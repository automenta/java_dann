package syncleus.dann.learn.deepbelief;

import java.util.Random;
import syncleus.dann.learn.autoencoder.HiddenLayer;
import syncleus.dann.learn.autoencoder.LogisticRegression;

public class DBN {

    public int trainingCases;
    public int n_ins;
    public int[] hidden_layer_sizes;
    public int n_outs;
    public int n_layers;
    public HiddenLayer[] sigmoid_layers;
    public RBM[] rbm_layers;
    public LogisticRegression log_layer;
    public Random rng;

    public static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.pow(Math.E, -x));
    }

    public DBN(int trainingCases, int n_ins, int[] hidden_layer_sizes, int n_outs, Random rng) {
        int input_size;

        this.trainingCases = trainingCases;
        this.n_ins = n_ins;
        this.hidden_layer_sizes = hidden_layer_sizes;
        this.n_outs = n_outs;
        this.n_layers = hidden_layer_sizes.length;

        this.sigmoid_layers = new HiddenLayer[n_layers];
        this.rbm_layers = new RBM[n_layers];

        if (rng == null) {
            this.rng = new Random(1234);
        } else {
            this.rng = rng;
        }

        // construct multi-layer
        for (int i = 0; i < this.n_layers; i++) {
            if (i == 0) {
                input_size = this.n_ins;
            } else {
                input_size = this.hidden_layer_sizes[i - 1];
            }

            // construct sigmoid_layer
            this.sigmoid_layers[i] = new HiddenLayer(this.trainingCases, input_size, this.hidden_layer_sizes[i], null, null, rng);

            // construct rbm_layer
            this.rbm_layers[i] = new RBM(this.trainingCases, input_size, this.hidden_layer_sizes[i], this.sigmoid_layers[i].W, this.sigmoid_layers[i].b, null, rng);
        }

        // layer for output using LogisticRegression
        this.log_layer = new LogisticRegression(this.trainingCases, this.hidden_layer_sizes[this.n_layers - 1], this.n_outs);
    }

    public void pretrain(final double[][] train_X, final double lr, final int k, final int epochs) {
        final double[][] layer_input = new double[n_layers+1][];
        //int prev_layer_input_size;
        //double[] prev_layer_input;

        layer_input[0] = new double[n_ins];
        for (int l = 1; l <= n_layers; l++) {
            layer_input[l] = new double[ hidden_layer_sizes[l - 1] ]; 
        }
        
        for (int i = 0; i < n_layers; i++) {  // layer-wise			
            for (int epoch = 0; epoch < epochs; epoch++) {  // training epochs
                for (int n = 0; n < trainingCases; n++) {  // input x1...xN
                    // layer input
                    int l;
                    for (l = 0; l <= i; l++) {

                        if (l == 0) {
                            System.arraycopy(train_X[n], 0, layer_input[l], 0, n_ins);
                        } else {
                            sigmoid_layers[l - 1].sample_h_given_v(layer_input[l-1], layer_input[l]);
                        }
                    }

                    rbm_layers[i].contrastive_divergence(layer_input[l-1], lr, k);
                }
            }
        }
    }

    public void finetune(double[][] train_X, double[][] train_Y, double lr, int epochs) {
        //TODO optimize like pretrain(), re-use layer_input variables
        
        double[] layer_input = new double[0];
        // int prev_layer_input_size;
        double[] prev_layer_input = new double[0];

        for (int epoch = 0; epoch < epochs; epoch++) {
            for (int n = 0; n < trainingCases; n++) {

                // layer input
                for (int i = 0; i < n_layers; i++) {
                    if (i == 0) {
                        prev_layer_input = new double[n_ins];
                        for (int j = 0; j < n_ins; j++) {
                            prev_layer_input[j] = train_X[n][j];
                        }
                    } else {
                        prev_layer_input = new double[hidden_layer_sizes[i - 1]];
                        for (int j = 0; j < hidden_layer_sizes[i - 1]; j++) {
                            prev_layer_input[j] = layer_input[j];
                        }
                    }

                    layer_input = new double[hidden_layer_sizes[i]];
                    sigmoid_layers[i].sample_h_given_v(prev_layer_input, layer_input);
                }

                log_layer.train(layer_input, train_Y[n], lr);
            }
            // lr *= 0.95;
        }
    }

    public void predict(double[] x, double[] y) {
        double[] layer_input = new double[0];
        // int prev_layer_input_size;
        double[] prev_layer_input = new double[n_ins];
        for (int j = 0; j < n_ins; j++) {
            prev_layer_input[j] = x[j];
        }

        double linear_output;

        // layer activation
        for (int i = 0; i < n_layers; i++) {
            layer_input = new double[sigmoid_layers[i].n_out];

            for (int k = 0; k < sigmoid_layers[i].n_out; k++) {
                linear_output = 0.0;

                for (int j = 0; j < sigmoid_layers[i].n_in; j++) {
                    linear_output += sigmoid_layers[i].W[k][j] * prev_layer_input[j];
                }
                linear_output += sigmoid_layers[i].b[k];
                layer_input[k] = sigmoid(linear_output);
            }

            if (i < n_layers - 1) {
                prev_layer_input = new double[sigmoid_layers[i].n_out];
                for (int j = 0; j < sigmoid_layers[i].n_out; j++) {
                    prev_layer_input[j] = layer_input[j];
                }
            }
        }

        for (int i = 0; i < log_layer.n_out; i++) {
            y[i] = 0;
            for (int j = 0; j < log_layer.n_in; j++) {
                y[i] += log_layer.W[i][j] * layer_input[j];
            }
            y[i] += log_layer.b[i];
        }

        log_layer.softmax(y);
    }

    private static void test_dbn() {
        Random rng = new Random(123);

        double pretrain_lr = 0.1;
        int pretraining_epochs = 1000;
        int k = 1;
        double finetune_lr = 0.1;
        int finetune_epochs = 500;

        int train_N = 6;
        int test_N = 4;
        int n_ins = 6;
        int n_outs = 2;
        int[] hidden_layer_sizes = {3, 3};

        // training data
        double[][] train_X = {
            {1, 1, 1, 0, 0, 0},
            {1, 0, 1, 0, 0, 0},
            {1, 1, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 0},
            {0, 0, 1, 1, 0, 0},
            {0, 0, 1, 1, 1, 0}
        };

        double[][] train_Y = {
            {1, 0},
            {1, 0},
            {1, 0},
            {0, 1},
            {0, 1},
            {0, 1},};

        // construct DBN
        DBN dbn = new DBN(train_N, n_ins, hidden_layer_sizes, n_outs, rng);

        // pretrain
        dbn.pretrain(train_X, pretrain_lr, k, pretraining_epochs);

        // finetune
        dbn.finetune(train_X, train_Y, finetune_lr, finetune_epochs);

        // test data
        double[][] test_X = {
            {1, 1, 0, 0, 0, 0},
            {1, 1, 1, 1, 0, 0},
            {0, 0, 0, 1, 1, 0},
            {0, 0, 1, 1, 1, 0},};

        double[][] test_Y = new double[test_N][n_outs];

        // test
        for (int i = 0; i < test_N; i++) {
            dbn.predict(test_X[i], test_Y[i]);
            for (int j = 0; j < n_outs; j++) {
                System.out.print(test_Y[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        test_dbn();
    }
}
