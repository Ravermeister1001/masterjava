package ru.javaops.masterjava.matrix;

import jdk.nashorn.internal.codegen.CompilerConstants;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];
        final int threadCount = Runtime.getRuntime().availableProcessors();
        final int maxIndex = matrixSize * matrixSize;
        final int cellsInThread = maxIndex / threadCount;
        final int[][] matrixBT = transpose(matrixB);

        Set<Callable<Boolean>> threads = new HashSet<>();
        int fromIndex = 0;
        for (int i = 1; i < threadCount; i++) {
            final int toIndex = i == threadCount ? maxIndex : fromIndex + cellsInThread;
            final int firstIndexResult = fromIndex;
            threads.add((Callable) () -> {
                for (int j = firstIndexResult; j < toIndex; j++) {
                    final int row = j / matrixSize;
                    final int col = j % matrixSize;

                    int sum = 0;
                    for (int k = 0; k < matrixSize; k++) {
                        sum += matrixA[row][k] * matrixBT[k][col];
                    }
                    matrixC[row][col] = sum;
                }
                return true;
            });
            fromIndex = toIndex;
        }
        executor.invokeAll(threads);
        return matrixC;
    }

    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        int [][] matrixBT = transpose(matrixB);

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixA[i][k] * matrixBT[j][k];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int[][] transpose(int[][] matrix) {
        for (int i = 0; i < matrix[0].length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[j][i] = matrix[i][j];
            }
        }
        return matrix;
    }
}
