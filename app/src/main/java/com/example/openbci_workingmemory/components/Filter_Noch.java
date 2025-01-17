package com.example.openbci_workingmemory.components;

import biz.source_code.dsp.filter.FilterCharacteristicsType;
import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignFisher;

// import java.util.Arrays; // For printing arrays when debugging

// Implements Butterworth filter coefficient generation and filter with DSP library
public class Filter_Noch {

    // ------------------------------------------------------------------------
    // Variables

    private static FilterPassType filterPassType;
    private static FilterCharacteristicsType filterCharacteristicsType;
    private static IirFilterCoefficients coeffs;
    private final int filterOrder;
    private final double samplingFrequency;
    private double[] b;
    private double[] a;
    private int nB;
    private int nA;

    // ------------------------------------------------------------------------
    // Constructor

    public Filter_Noch(double samplingFrequency, String filterType, int filterOrder, double fc1, double fc2) {
        // samplingRate = sampling frequency of EEG (220hz or 256hz for Muse)
        // filterType = lowpass, highpass, bandstop, or bandpass
        // filterOrder = filter order corresponding to degree of approximating polynomial. Higher order might increase ripples
        // fc1 = primary cut-off frequency for lowpass/highpass. Low cut-off for bandstop or bandpass
        // fc2 = high cut-off frequency for bandstop/bandpass. Ignored for low/high pass

        filterCharacteristicsType = FilterCharacteristicsType.butterworth;

        if (filterType.contains("lowpass")) {
            filterPassType = FilterPassType.lowpass;

        } else if (filterType.contains("highpass")) {
            filterPassType = FilterPassType.highpass;

        } else if (filterType.contains("bandstop")) {
            filterPassType = FilterPassType.bandstop;

        } else if (filterType.contains("bandpass")) {
            filterPassType = FilterPassType.bandpass;

        } else {
            throw new RuntimeException("Filter type not recognized.");
        }

        this.filterOrder = filterOrder;
        this.samplingFrequency = samplingFrequency;

        coeffs = IirFilterDesignFisher.design(filterPassType, filterCharacteristicsType, filterOrder, 0., fc1 / samplingFrequency, fc2 / samplingFrequency);

        //a     1	    -4.76174050667987	9.07509931084016	-8.65282300936090	4.12739951130037	-0.787933380651263
        //b     6017026,58	30085132,90	60170265,80	60170265,80	30085132,90	6017026,58

        //b = coeffs.b;
        //a = coeffs.a;



        b = new double[]{1.70396779800575e-05,0,-5.11190339401724e-05,0,5.11190339401724e-05,0,-1.70396779800575e-05};
        //b = new double[]{6017026.58, 30085132.90, 60170265.80, 60170265.80, 30085132.90, 6017026.58};
        a = new double[]{1,-1.13797860024079e-15,2.89529217787790,-2.18533752303564e-15,2.79599458428336,-1.05028695535534e-15,0.900566088981624};


        //b = new double[]{0.00936885367995560,0,-0.0562131220797336,0,0.140532805199334,0,-0.187377073599112,0,0.140532805199334,0,-0.0562131220797336,0,0.00936885367995560};
        //b = new double[]{6017026.58, 30085132.90, 60170265.80, 60170265.80, 30085132.90, 6017026.58};
       // a = new double[]{1,-7.15096357676174,23.2719091715497,-46.0967736856897,62.6717328860056,-62.1870528982273,46.3001185830581,-25.9916766919261,10.8962828593903,-3.33194193315429,0.703645643079020,-0.0909469369758924,0.00566657982016370};

        nB = b.length;
        nA = a.length;
    }

    // ---------------------------------------------------------------------
    // Methods

    public double[] transform(double x, double[] z) {
        // This function implements the Discrete Form II Transposed of
        // a linear filter.
        //
        // Args:
        //  x: the current sample to be filtered
        //  z: the internal state of the filter
        //
        // Returns:
        //  the updated internal state of the filter, with the new
        //  filtered value in the last position. This is a hack
        //  that allows to pass both the internal state and the
        //  output of the filter at once.

        // Esta función implementa la forma discreta II transpuesta de
        // un filtro lineal.
        //
        // Args:
        // x: la muestra actual a filtrar
        // z: el estado interno del filtro
        //
        // Devoluciones:
        // el estado interno actualizado del filtro, con el nuevo
        // valor filtrado en la última posición. Esto es un truco
        // que permite pasar tanto el estado interno como el
        // salida del filtro a la vez.

        z[z.length - 1] = 0;
        double y = b[0] * x + z[0];
        for (int i = 1; i < nB; i++) {
            z[i - 1] = b[i] * x + z[i] - a[i] * y;
        }

        z[z.length - 1] = y;

        return z;

    }

    public double[][] transform(double[] x, double[][] z) {
        // This function implements the Discrete Form II Transposed of
        // a linear filter for multichannel signals
        //
        // Args:
        //  x: the current channel samples to be filtered
        //  z: the internal state of the filter for each channels [nbCh,nbPoints]
        //
        // Returns:
        //  the updated internal states of the filter, with the new
        //  filtered values in the last position. [nbCh,nbPoints]
        //  This is a hack that allows to pass both the internal state and the
        //  output of the filter at once.

        // double[] zNew = new double[z[0].length];


        // Esta función implementa la forma discreta II transpuesta de
        // un filtro lineal para señales multicanal
        //
        // Args:
        // x: las muestras del canal actual a filtrar
        // z: el estado interno del filtro para cada canal [nbCh, nbPoints]
        //
        // Devoluciones:
        // los estados internos actualizados del filtro, con el nuevo
        // valores filtrados en la última posición. [nbCh, nbPoints]
        // Este es un truco que permite pasar tanto el estado interno como el
        // salida del filtro a la vez.

        // doble [] zNew = nuevo doble [z [0] .length];


        for (int i = 0; i < 8; i++) {       // Careful!
            z[i] = transform(x[i], z[i]);
        }

        return z;

    }

    public static double[] extractFilteredSamples(double[][] z) {
        // Utility function to extract the filtered samples from the returned array
        // of transform()
        int len = z.length;

        double[] filtSignal = new double[len];     // TODO can this instantiation be avoided?
        for (int i = 0; i < len; i++) {
            filtSignal[i] = z[i][z[0].length - 1];
        }
        return filtSignal;
    }

    public static double[] extractFilteredSamplesProcessed(double[][] z,int maxSignalFrequency,int minSignalFrequency,float meanSignalFrecuency) {
        // Utility function to extract the filtered samples from the returned array
        // of transform()
        int len = z.length;
        double[] filtSignal = new double[len];     // TODO can this instantiation be avoided?
        for (int i = 0; i < len; i++) {
            filtSignal[i] = (z[i][z[0].length - 1] - meanSignalFrecuency) / (maxSignalFrequency-meanSignalFrecuency);
        }
        return filtSignal;
    }

    public static double extractFilteredSamples(double[] z) {
        // Utility function to extract last value from array
        return z[z.length - 1];
    }

    public int getNB() {
        return nB;
    }

    public int getNA() {
        return nA;
    }

    public void updateFilter(int fc1, int fc2) {
        coeffs = IirFilterDesignFisher.design(filterPassType, filterCharacteristicsType, filterOrder, 0., fc1 / samplingFrequency, fc2 / samplingFrequency);

        b = coeffs.b;
        a = coeffs.a;

        nB = b.length;
        nA = a.length;
    }

}