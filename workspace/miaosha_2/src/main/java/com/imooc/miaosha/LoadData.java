package com.imooc.miaosha;

import com.csvreader.CsvReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static java.io.File.separatorChar;

public class LoadData {

    public static void main(String[] args) throws Exception {

        String a = "";
        new String(a.getBytes("utf-8") , "utf-8");

        Map pc = importCsv("F:/DOC/us_cities_sim_0727.csv");
        String pcs = pc.keySet().toString();
//        System.out.println(pcs);
        try (BufferedWriter out = new BufferedWriter(new FileWriter(
                new File("F:/DOC/us_cities_temp.json")))) {
            out.write(pcs, 0, pcs.length());
//            Iterator it = pc.keySet().iterator();
//            while (it.hasNext()) {
//                String str = it.next().toString();
//                out.write(str, 0, str.length());
//                out.newLine();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map importCsv(String filePath) throws Exception {
        CsvReader reader = null;
        List<String[]> inputList = new ArrayList<>();
        Province province = null;
        City city;
        Map pc = new HashMap();
        List cities = new ArrayList();
        int cnt = 0;
        try {
            reader = new CsvReader(filePath, separatorChar, Charset.forName("UTF-8"));

            // 读取表头  加上这一句是不算表头数据从第二行开始取
            reader.readHeaders();
            // 逐条读取记录，直至读完
            while (reader.readRecord()) {
                cnt++;
                String[] inputVals = reader.getRawRecord().split(",");
                inputList.add(inputVals);
                if (inputVals[3].replaceAll("\"", "").equals("0") &&
                        inputVals[2].replaceAll("\"", "").equals("0")) {
                    // 省市都为0 是国家 跳过
                } else if (inputVals[3].replaceAll("\"", "").equals("0")) { // 省
                    if (province != null && cities.size() > 0) {
                        province.setCitys(cities);
                        pc.put(province, cities);

                        province = new Province(inputVals[4], inputVals[0], inputVals[1]);
                        cities = new ArrayList();
                    } else {
                        province = new Province(inputVals[4], inputVals[0], inputVals[1]);
                    }
                } else {
                    city = new City(inputVals[4], inputVals[0], inputVals[1]);
                    cities.add(city);
                }

            }
            province.setCitys(cities);
            pc.put(province, cities);
        } catch (Exception e) {
            System.out.println("读取CSV出错..." + cnt + e);
            throw e;
        } finally {
            if (null != reader) {
                reader.close();
            }
        }
        return pc;
    }
}
