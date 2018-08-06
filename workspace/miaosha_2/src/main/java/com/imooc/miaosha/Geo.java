package com.imooc.miaosha;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.io.File.separatorChar;

/**
 * java.util.LocaleISOData
 * https://www.cnblogs.com/steven-snow/p/9182931.html
 * https://blog.csdn.net/stemq/article/details/60780859
 */
public class Geo {

    /**
     * java导入csv文件
     *
     * @param filePath 导入路径
     * @return
     * @throws Exception
     */
    public static List<String[]> importCsv(String filePath) throws Exception {
        CsvReader reader = null;
        List<String[]> inputList = new ArrayList<>();
        List<String[]> outputList = new ArrayList<>();
        try {
            reader = new CsvReader(filePath, separatorChar, Charset.forName("UTF-8"));
            // 读取表头  加上这一句是不算表头数据从第二行开始取
            reader.readHeaders();
            // 逐条读取记录，直至读完
            while (reader.readRecord()) {
                String[] inputVals = reader.getRawRecord().split(",");
                inputList.add(inputVals);

                String[] expandValues = expandArray(inputVals);
                outputList.add(expandValues);
            }
        } catch (Exception e) {
            System.out.println("读取CSV出错..." + e);
            throw e;
        } finally {
            if (null != reader) {
                reader.close();
            }
        }
        return outputList/*inputList*/;
    }

    private static String[] expandArray(String[] inputVals) {
        int outputLength = outCols.split(",").length;
        String[] outputVals = new String[outputLength];
        String province = "";
        String cityCntStr = "0";

        for (int i = 0; i < 3; i++) {
            if (inputVals[i].contains("\"")) {
                outputVals[i] = inputVals[i].replaceAll("\"", "");
            } else {
                outputVals[i] = inputVals[i];
            }
//            if(i == 0) {
//                outputVals[i] = inputVals[i].replaceAll("\"", "");
//            } else if(i == 1) { // 英文 6 4 1
//                outputVals[i] = inputVals.length > 6 ? inputVals[6].replaceAll("\"", "") : (
//                        inputVals.length > 4 ? inputVals[4].replaceAll("\"", "") :
//                                inputVals[1].replaceAll("\"", "")
//                        );
//            } else if(i == 2) { // 中文 7 6 5 2
//                outputVals[i] = inputVals.length > 7 ? inputVals[7].replaceAll("\"", "") : (
//                        inputVals.length > 6 ? "" : (
//                                inputVals.length > 5 ? inputVals[5].replaceAll("\"", "") :
//                                        inputVals[2].replaceAll("\"", "")
//                                )
//                        );
//            }
        }
        for (int i = 3; i < outputLength; i++) {
            // 第四位是省级代码
            if (i == 3 && inputVals.length > 3 && inputVals[i] != null) {
                province = inputVals[i];
                stateCityMap.put(province, stateCityMap.get(province) == null ? cityCntStr :
                        Integer.toString(new AtomicInteger(Integer.valueOf(stateCityMap.get(province))).incrementAndGet())
                );
                // 设置省级代码
                outputVals[i] = province.replaceAll("\"", "");
//            } else if (i == outputLength - 3) { // 设置省级代码
//                outputVals[i] = province.replaceAll("\"", "");
            } else if (i == 4) { // 设置省级名称
                outputVals[i] = inputVals.length > 4 ? inputVals[4].replaceAll("\"", "") : "";
            } else if (i == 5) { // 设置城市名称
                outputVals[i] = inputVals.length > 6 ? inputVals[6].replaceAll("\"", "") : "";
            } else if (i == 6) { // 设置省级序号
                outputVals[i] = Integer.toString(stateCityMap.size());
            } else if (i == 7) { // 设置城市序号
                cityCntStr = stateCityMap.get(province);
                outputVals[i] = cityCntStr;
            } else {
                outputVals[i] = "";
            }
        }
//        for (int i = 3; i < outputLength; i++) {
//            // 第四位是省级代码
//            if (i == 3 && inputVals.length > 3 && inputVals[i] != null) {
//                province = inputVals[i];
//                stateCityMap.put(province, stateCityMap.get(province) == null ? cityCntStr :
//                        Integer.toString(new AtomicInteger(Integer.valueOf(stateCityMap.get(province))).incrementAndGet())
//                );
//                // 设置省级序号
//                outputVals[i] = Integer.toString(stateCityMap.size());
//            } else if (i == 4) { // 设置城市序号
//                cityCntStr = stateCityMap.get(province);
//                outputVals[i] = cityCntStr;
//            } else {
//                outputVals[i] = "";
//            }
//        }
        return outputVals;
    }

    public static void writeCSV(List<String[]> dataList) {
        // 定义一个CSV路径
        String csvFilePath = "F:/DOC/us_cities_order.csv";
        try {
            // 创建CSV写对象 例如:CsvWriter(文件路径，分隔符，编码格式);
            CsvWriter csvWriter = new CsvWriter(csvFilePath, ',', Charset.forName("UTF-8"));
            // 写表头
            String[] csvHeaders = outCols.split(","); // { "编号", "姓名", "年龄" };
            csvWriter.writeRecord(csvHeaders);
            // 写内容
            for (int i = 0; i < dataList.size(); i++) {
                String[] csvContent = dataList.get(i);// { i + "000000", "StemQ", "1" + i };
                csvWriter.writeRecord(csvContent);
            }
            csvWriter.close();
            System.out.println("--------CSV文件已经写入--------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String inCols = "country_iso_code, country_name_en, country_name_cn, subdivision_iso_code, subdivision_name_en, subdivision_name_cn, city_name_en, city_name_cn";
    private static String outCols = "country_iso, country_name_en, country_name_cn, subdivision_iso, subdivision_name_en, city_name_en, subdivision_seq, city_seq";
    private static Map<String, String> stateCityMap = new HashMap<>();

    public static void main(String[] args) throws Exception {
        String filePath = "F:/DOC/us_cities.csv"; // us_cities.csv
        // 测试导入
        List<String[]> data = importCsv(filePath);
        // 测试导出
        writeCSV(data);
    }
}
