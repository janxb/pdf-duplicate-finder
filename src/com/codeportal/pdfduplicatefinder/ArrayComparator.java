package com.codeportal.pdfduplicatefinder;

public class ArrayComparator {
	public static int compareByteArray(byte[] a, byte[] b) {
		int n = Math.min(a.length, b.length), nLarge = Math.max(a.length, b.length);
		int unequalCount = nLarge - n;
		for (int i = 0; i < n; i++)
			if (a[i] != b[i])
				unequalCount++;
		Long percentage = Math.round(100 - unequalCount * 100.0 / nLarge);
		return percentage.intValue();
	}

	public static int compareIntArray(int[] a, int[] b) {
		int n = Math.min(a.length, b.length), nLarge = Math.max(a.length, b.length);
		int unequalCount = nLarge - n;
		for (int i = 0; i < n; i++)
			if (a[i] != b[i])
				unequalCount++;
		Long percentage = Math.round(100 - unequalCount * 100.0 / nLarge);
		return percentage.intValue();
	}
}
