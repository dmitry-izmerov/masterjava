package ru.javaops.masterjava.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor, int poolSize) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

		List<Future<?>> futures = new ArrayList<>(poolSize);
//		List<Callable<Object>> tasks = new ArrayList<>(poolSize);
		int rangeSize = matrixSize / poolSize;
		for (int i = 0; i < matrixSize; i += rangeSize) {
			futures.add(executor.submit(new MatrixMultiplyTask(matrixA, matrixB, matrixC, i, rangeSize)));
			//tasks.add(Executors.callable(new MatrixMultiplyTask(matrixA, matrixB, matrixC, i, rangeSize)));
		}

		for (Future<?> future : futures) {
			future.get();
		}
		//executor.invokeAll(tasks);

		return matrixC;
    }

    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
		final int matrixSize = matrixA.length;
		final int[][] matrixC = new int[matrixSize][matrixSize];
		multiply(matrixA, matrixB, matrixC, 0, matrixA.length);
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

	// its optimized by using advices from post below
	// @see https://habrahabr.ru/post/114797/
    private static void multiply(int[][] matrixA, int[][] matrixB, int[][] matrixC, int startRowInclusive, int endRowExclusive) {
		final int matrixSize = matrixA.length;
		final int[] transponsedColumn = new int[matrixSize];

		for (int rowNum = startRowInclusive; rowNum < endRowExclusive; rowNum++) {

			for (int k = 0; k < matrixSize; k++) {
				transponsedColumn[k] = matrixB[k][rowNum];
			}
			for (int j = 0; j < matrixSize; j++) {
				int[] row = matrixA[rowNum];
				int sum = 0;
				for (int k = 0; k < matrixSize; k++) {
					sum += row[k] * transponsedColumn[k];
				}
				matrixC[rowNum][j] = sum;
			}
		}
	}

	private static class MatrixMultiplyTask implements Runnable {

		private int[][] matrixA;
		private int[][] matrixB;
		private int[][] matrixC;
		private int i;
		private int rangeSize;

		MatrixMultiplyTask(int[][] matrixA, int[][] matrixB, int[][] matrixC, int i, int rangeSize) {
			this.matrixA = matrixA;
			this.matrixB = matrixB;
			this.matrixC = matrixC;
			this.i = i;
			this.rangeSize = rangeSize;
		}

		@Override
		public void run() {
			int endRow = i + rangeSize;
			multiply(matrixA, matrixB, matrixC, i, endRow);
		}
	}
}
