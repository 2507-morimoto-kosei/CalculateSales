package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		//コマンドライン引数が渡されているか確認
		if(args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();
		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		//コマンドライン引数で渡されたパスのファイルを配列として格納
		File[] files = new File(args[0]).listFiles();

		//今後扱うファイルを格納するArrayList準備
		List<File> rcdFiles = new ArrayList<>();

		//filesに格納されたファイル(ディレクトリ)が条件にあっているか判定
		for(int i = 0; i < files.length; i++) {
			String filesName = files[i].getName();
			//ファイルかつファイル名が「数字8桁.rcd」だったらrcdFilesに書き込み
			if(files[i].isFile() && filesName.matches("^[0-9]{8}[.]rcd$")) {
				rcdFiles.add(files[i]);
			}
		}
		//今後扱うファイルを格納したArrayListのソート
		Collections.sort(rcdFiles);

		//条件に合ったファイルが連番になっているか確認
		for(int i = 0; i < rcdFiles.size() -1; i++) {
			//2つのファイル名から数字を切り出しint型に変換
			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));
			//ファイル名を比較して連番になっているか確認
			if((latter - former) != 1) {
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}
		//rcdFilesに格納されているファイルを一個ずつ見ていく
		BufferedReader br = null;
		String line;
		for(int i = 0; i < rcdFiles.size(); i++) {
			try {
				List<String> lines = new ArrayList<>();
				File file = rcdFiles.get(i);
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				// 一行ずつ取り出してリストに追加
				while((line = br.readLine()) != null) {
					lines.add(line);
				}
				//売上ファイルの中身が3行以上あるか確認
				if(lines.size() != 2) {
					System.out.println(rcdFiles.get(i).getName() + "のフォーマットが不正です");
					return;
				}
				//支店コードに対応する支店名があるか確認する
				if(!branchNames.containsKey(lines.get(0))) {
					System.out.println(rcdFiles.get(i).getName() + "の支店コードが不正です");
					return;
				}
				//売上ファイルの売上金額が数字であるか確認
				if(!lines.get(1).matches("^[0-9]+$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}
				//店の売り上げをlong型にキャスト
				long fileSale = Long.parseLong(lines.get(1));

				//売上金額の加算
				Long saleAmount = branchSales.get(lines.get(0)) + fileSale;

				//売上金額の合計が11桁以上になってないか確認
				if(saleAmount >= 10000000000L) {
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				//加算した売上金額をMapに追加
				branchSales.put(lines.get(0), saleAmount);

			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}
		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}
	}
	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;
		try {
			File file = new File(path, fileName);
			//支店定義ファイルの存在確認
			if(!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");
				//支店定義ファイルのフォーマット確認
				if((items.length != 2) || (!items[0].matches("^[0-9]{3}"))) {
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], (long)0);
			}
		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;
		try {
			File sum = new File(path, fileName);
			FileWriter fw = new FileWriter(sum);
			bw = new BufferedWriter(fw);
			for(String key : branchNames.keySet()) {
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				bw.newLine();
			}
		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}
}