package org.example;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import org.apache.commons.math3.linear.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class AHPApplication {
	public static void main(String[] args) throws Exception {
		ArrayList<Double> distanceWeights = new ArrayList<>();
		ArrayList<Double> payWeights = new ArrayList<>();
		ArrayList<Double> rankWeights = new ArrayList<>();
		ArrayList<Double> chooseWeights = new ArrayList<>();
		ArrayList<Double> variableWeights = new ArrayList<>();

		Scanner scanner = new Scanner(System.in);
		System.out.println("請輸入居住地:");

		//1.計算地點權重
		String location1 = scanner.nextLine();
		System.out.println("地點權重");
		distanceWeights = distance(location1, distanceWeights);

		//2計算學費權重
		System.out.println("學費權重");
		payWeights = pay(payWeights);

		//3計算學校排名權重
		System.out.println("學校排名權重");
		rankWeights = rank(rankWeights);

		//4科系喜好程度
		System.out.println("請輸入科系喜好程度偏好程度資管,資工,電機,數學分數越高越重要(1~9),請使用逗號分隔");
		String choose = scanner.nextLine();
		String[] o = choose.split(",");
		System.out.println("科系喜好權重");
		chooseWeights = choose(o, chooseWeights);

		System.out.println("請輸入地點,學費,學校排名,科系喜好程度偏好程度,分數越高越重要(1~9),請使用逗號分隔");
		String point = scanner.nextLine();
		String[] t = point.split(",");
		System.out.println("變數權重");
		variableWeights = variable(t, variableWeights);


		//計算權重
		Double first = variableWeights.get(0).doubleValue() * distanceWeights.get(0).doubleValue()
				+ variableWeights.get(1).doubleValue() * payWeights.get(0).doubleValue()
				+ variableWeights.get(2).doubleValue() * rankWeights.get(0).doubleValue()
				+ variableWeights.get(3).doubleValue() * chooseWeights.get(0).doubleValue();

		Double second = variableWeights.get(0).doubleValue() * distanceWeights.get(1).doubleValue()
				+ variableWeights.get(1).doubleValue() * payWeights.get(1).doubleValue()
				+ variableWeights.get(2).doubleValue() * rankWeights.get(1).doubleValue()
				+ variableWeights.get(3).doubleValue() * chooseWeights.get(1).doubleValue();

		Double third = variableWeights.get(0).doubleValue() * distanceWeights.get(2).doubleValue()
				+ variableWeights.get(1).doubleValue() * payWeights.get(2).doubleValue()
				+ variableWeights.get(2).doubleValue() * rankWeights.get(2).doubleValue()
				+ variableWeights.get(3).doubleValue() * chooseWeights.get(2).doubleValue();

		Double fourth = variableWeights.get(0).doubleValue() * distanceWeights.get(3).doubleValue()
				+ variableWeights.get(1).doubleValue() * payWeights.get(3).doubleValue()
				+ variableWeights.get(2).doubleValue() * rankWeights.get(3).doubleValue()
				+ variableWeights.get(3).doubleValue() * chooseWeights.get(3).doubleValue();

		// 创建权重和名称的列表
		List<WeightedSchool> schools = Arrays.asList(
				new WeightedSchool("元智資管系", first),
				new WeightedSchool("中原資工系", second),
				new WeightedSchool("淡江電機系", third),
				new WeightedSchool("輔大數學系", fourth)
		);

		// 正排序
		List<WeightedSchool> sortedSchools = schools.stream()
				.sorted()
				.collect(Collectors.toList());

		// 逆排序
//        List<WeightedSchool> sortedSchools = schools.stream()
//                .sorted(Comparator.reverseOrder())
//                .collect(Collectors.toList());
		// 打印排序后的结果
		sortedSchools.forEach(school ->
				System.out.println(school.name + "總權重=" + school.weight)
		);

		// 计算总权重
		Double total = first + second + third + fourth;
		System.out.println("權重總和=" + total);
	}

	public static class WeightedSchool implements Comparable<WeightedSchool> {
		String name;
		Double weight;

		WeightedSchool(String name, Double weight) {
			this.name = name;
			this.weight = weight;
		}

		@Override
		public int compareTo(WeightedSchool other) {
			// 逆序排列，以便最大的权重在前
			return other.weight.compareTo(this.weight);
		}
	}

	private static ArrayList<Double> rank(ArrayList<Double> arrayList) {
		//元智:
		Double a = 6.00;
		//中原:
		Double b = 4.00;
		//淡江:
		Double c = 1.00;
		//輔大:
		Double d = 2.00;
		double[][] matrix = {
				{1, b / a, c / a, d / a},
				{a / b, 1, c / b, d / b},
				{a / c, b / c, 1, d / c},
				{a / d, b / d, c / d, 1}
		};
		//AHP換算
		arrayList = AHP(matrix, arrayList);
		return arrayList;
	}

	private static ArrayList<Double> choose(String[] o, ArrayList<Double> arrayList) {
		double aa = Double.valueOf(o[0]);
		double bb = Double.valueOf(o[1]);
		double cc = Double.valueOf(o[2]);
		double dd = Double.valueOf(o[3]);
		double[][] matrix0 = {
				{1, aa / bb, aa / cc, aa / dd},
				{bb / aa, 1, bb / cc, bb / dd},
				{cc / aa, cc / bb, 1, cc / dd},
				{dd / aa, dd / bb, dd / cc, 1}
		};
		arrayList = AHP(matrix0, arrayList);

		return arrayList;
	}

	private static ArrayList<Double> variable(String[] t, ArrayList<Double> arrayList) {
		double a = Double.valueOf(t[0]);
		double b = Double.valueOf(t[1]);
		double c = Double.valueOf(t[2]);
		double d = Double.valueOf(t[3]);

		double[][] matrix = {
				{1, a / b, a / c, a / d},
				{b / a, 1, b / c, b / d},
				{c / a, c / b, 1, c / d},
				{d / a, d / b, d / c, 1}
		};
		arrayList = AHP(matrix, arrayList);

		return arrayList;
	}

	private static ArrayList<Double> distance(String local, ArrayList<Double> arrayList) {
		String location2;
		//元智:桃園縣遠東路135號
		String a = "桃園縣遠東路135號";
		//中原:桃園縣中壢市中北路200號
		String b = "桃園縣中壢市中北路200號";
		//淡江:新北市淡水區英專路151號
		String c = "新北市淡水區英專路151號";
		//輔仁:新北市新莊區中正路510號
		String d = "新北市新莊區中正路510號";
		double distance_a = calculateDistance(local, a);
		double distance_b = calculateDistance(local, b);
		double distance_c = calculateDistance(local, c);
		double distance_d = calculateDistance(local, d);

		double[][] matrix = {
				{1, distance_b / distance_a, distance_c / distance_a, distance_d / distance_a},
				{distance_a / distance_b, 1, distance_c / distance_b, distance_d / distance_b},
				{distance_a / distance_c, distance_b / distance_c, 1, distance_d / distance_c},
				{distance_a / distance_d, distance_b / distance_d, distance_c / distance_d, 1}
		};
		//AHP換算
		arrayList = AHP(matrix, arrayList);
		return arrayList;
	}

	private static ArrayList<Double> pay(ArrayList<Double> arrayList) {
		//元智:
		Double a = 56239.00;
		//中原:
		Double b = 55550.00;
		//淡江:
		Double c = 40800.00;
		//輔仁:
		Double d = 54890.00;
		double[][] matrix = {
				{1, b / a, c / a, d / a},
				{a / b, 1, c / b, d / b},
				{a / c, b / c, 1, d / c},
				{a / d, b / d, c / d, 1}
		};
		//AHP換算
		arrayList = AHP(matrix, arrayList);
		return arrayList;
	}

	private static ArrayList<Double> AHP(double[][] matrix, ArrayList<Double> arrayList) {
		AHPApplication ahp = new AHPApplication();
		double[] weights = ahp.calculateWeights(matrix);

		// 輸出計算得到的權重
		System.out.println("Calculated Weights:");
		for (double weight : weights) {
			System.out.println(weight);
			arrayList.add(weight);
		}
		return arrayList;
	}

	private static double calculateDistance(String origin, String destination) {
		try {
			GeoApiContext context = new GeoApiContext.Builder()
					.apiKey("AIzaSyCp0qEeDIvhDCQ2Z-mgJhPrbsXyv63zlTI")
					.build();

			DirectionsApiRequest request = DirectionsApi.getDirections(context, origin, destination)
					.mode(TravelMode.DRIVING);  // 或者使用 TravelMode.WALKING, TravelMode.BICYCLING，TravelMode.TRANSIT

			DirectionsRoute[] routes = request.await().routes;
			if (routes.length > 0) {
				return routes[0].legs[0].distance.inMeters;
			} else {
				System.err.println("No route found between the locations.");
				return -1.0;
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return -1.0;
		} catch (ApiException e) {
			throw new RuntimeException(e);
		}
	}

	// 計算權重
	private double[] calculateWeights(double[][] matrix) {
		int n = matrix.length;

		// 将判定矩阵转换为实数矩阵
		RealMatrix realMatrix = new Array2DRowRealMatrix(matrix);

		// 计算特征向量
		double[] eigenVector = calculateEigenVector(realMatrix);

		// 计算一致性指标
		double consistencyIndex = calculateConsistencyIndex(realMatrix, eigenVector);

		// 如果一致性指标超过某个阈值，可能需要重新考虑输入的判定矩阵
		System.out.println("Consistency Index: " + consistencyIndex);

		// 归一化特征向量得到权重
		normalizeWeights(eigenVector);

		return eigenVector;
	}

	// 计算特征向量
	private double[] calculateEigenVector(RealMatrix matrix) {
		EigenDecomposition decomposition = new EigenDecomposition(matrix);
		// 获取特征值对应的索引
		int indexOfMaxEigenvalue = findIndexOfMaxValue(decomposition.getRealEigenvalues());

		// 获取特征向量
		double[] eigenVector = decomposition.getEigenvector(indexOfMaxEigenvalue).toArray();

		return eigenVector;
	}

	// 辅助方法：找到数组中最大值的索引
	private int findIndexOfMaxValue(double[] array) {
		int maxIndex = 0;
		double maxValue = array[0];

		for (int i = 1; i < array.length; i++) {
			if (array[i] > maxValue) {
				maxIndex = i;
				maxValue = array[i];
			}
		}

		return maxIndex;
	}

	// 计算一致性指标
	private double calculateConsistencyIndex(RealMatrix matrix, double[] eigenVector) {
		double lambdaMax = eigenVector[eigenVector.length - 1];
		int n = matrix.getRowDimension();
		return (lambdaMax - n) / (n - 1);
	}

	// 归一化特征向量得到权重
	private void normalizeWeights(double[] eigenVector) {
		double sum = 0;
		for (double value : eigenVector) {
			sum += value;
		}

		// 归一化
		for (int i = 0; i < eigenVector.length; i++) {
			eigenVector[i] /= sum;
		}
	}
}