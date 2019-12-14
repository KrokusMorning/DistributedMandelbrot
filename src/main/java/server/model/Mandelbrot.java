package server.model;

import org.apache.commons.math3.complex.Complex;

/**
 * Class used to generate pgm encoded mandelbrot representations of intervals of complex numbers.
 */
public class Mandelbrot {
    private Double min_c_re;
    private Double min_c_im;
    private Double max_c_re;
    private Double max_c_im;
    private int x;
    private int y;
    private int inf_n;
    private Complex cMax;
    private Complex cMin;
    private final int COLORS = 256;
    private int colorStep;

    public Mandelbrot() {

    }

    public Mandelbrot(Double min_c_re, Double min_c_im, Double max_c_re, Double max_c_im, int x, int y, int inf_n) {
        this.min_c_re = min_c_re;
        this.min_c_im = min_c_im;
        this.max_c_re = max_c_re;
        this.max_c_im = max_c_im;
        this.x = x;
        this.y = y;
        this.inf_n = inf_n;
        cMax = new Complex(max_c_re, max_c_im);
        cMin = new Complex(min_c_re, min_c_im);
        this.colorStep = COLORS / inf_n;
    }

    /**
     * Generates a pgm encoded pixel representation of the mandelbrot values in the provided range.
     * Four values ara added to the first part of the string to identify the piece when assembling the final pgm.
     * Iterates over the provided range and tests if the complex number found within the range are part of the mandelbrot set.
     * The number of steps in the range are determined by the number of pixels.
     * @return a string containing a pgm encoded mandelbrot range.
     */
    public String generateMandelbrot(){

        StringBuilder pgmData = new StringBuilder();
        double xStep = (max_c_re-min_c_re)/x;
        double yStep = (max_c_im-min_c_im)/y;
        Complex c = new Complex(min_c_re, min_c_im);
        pgmData.append(min_c_im).append(" ").append(min_c_re).append(" ").append(max_c_re).append(" ").append(max_c_im).append(" ");
        for(int i = 0; i < y; i++){
            for(int j = 0; j < x; j++){
                pgmData.append(test(c));
                c = new Complex(c.getReal() + xStep, c.getImaginary());
            }
            c = new Complex(min_c_re, c.getImaginary() + yStep);
        }
        return pgmData.toString();
    }

    /**
     * Test if a complex number is in the mandelbrot range
     * @param c, a complec number that is tested for mandelbrot.
     * @return a String containing a value for pgm representation of the mandelbrot status of the provided complex number.
     */
    private String test(Complex c){
        String isMandelbrot = 256 + " ";
        Complex z = new Complex(0);
        Complex z1 = new Complex(0);
        for(int i = 0; i < inf_n; i++){
            z = z1;
            z1 = c.add(z.multiply(z));
            if(z1.abs() >= 2) {
                isMandelbrot = i % 256 +" ";
                break;
            }
        }
        return isMandelbrot;
    }
}
