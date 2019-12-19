package cn.me.excel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * @Classname CommonUtils
 * @Description TODO
 * @Date 2019/12/18 10:17
 * @Created by yuhousheng
 */
public class CommonUtils {

    /**
     * 功能：返回要入库的excel文件全路径
     *
     * @param path2Excels excel文件名称
     * @param path2ExcelsPrefix excel文件所在的目录名称
     * @return excel文件的全路径
     */
    public static List<String> getExcelFiles(String path2Excels,String path2ExcelsPrefix) {
        List<String> excelFiles = new ArrayList<String>();
        try {
            FileReader fr = new FileReader(path2Excels);
            BufferedReader br = new BufferedReader(fr);
            String excelName = br.readLine();
            while (excelName!=null) {
                boolean notValidExcelFile = excelName.startsWith("#");
                if (!notValidExcelFile) { // 只读取没有被注释掉的excel文件
                    String fullExcelName = path2ExcelsPrefix + "\\" + excelName;
                    excelFiles.add(fullExcelName);
                }
                excelName=br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return excelFiles;
    }


    /**
     * 功能：获取所有行数据
     * @param sheet sheet页
     * @param collumnNum 一行有多少列
     * @return 所有行的数据
     */
    public static List<List<String>> getCellValue(Sheet sheet,int collumnNum) {
        List<List<String>> rows = new ArrayList<List<String>>();
        for (Row row:sheet) {
            List<String> collumnValues = new ArrayList<String>();
            if (null != row && 0 == row.getRowNum()) { // 跳过第一行的标题
                continue;
            }

            // 遍历每行的所有列，使其组成一行 例如：aa,bb,cc
            StringBuilder sb = new StringBuilder();
            for (int i=0;i<collumnNum;i++) {
                String value = row.getCell(i).getStringCellValue();
                if (null==value) {
                    value="-";
                }
                collumnValues.add(value);
            }
            rows.add(collumnValues);
        }
        return rows;
    }


    /**
     * 功能： 对日期进行加减
     * @param localDate
     * @param amountToAdd
     * @return
     */
    public static String getDateStr(LocalDate localDate , Long amountToAdd) {
        LocalDate plus = localDate.plus(amountToAdd, ChronoUnit.DAYS);
        return plus.toString();
    }


    /**
     *  产生最近60天的日期，包括今天日期
     * @return 最近60天的日期 yyyy-MM-dd
     */
    public static List<String> get60DateStrs() {
        List<String> dts = new ArrayList<String>();
        LocalDate now = LocalDate.now();
        String yestoday = getDateStr(now, -1L);
        dts.add(yestoday);//昨日日期
        dts.add(now.toString()); // 今日日期
        for (int i=1;i<=60;i++) {
            Long amount = Long.valueOf(String.valueOf(i));
            String dtTemp = getDateStr(now, amount);
            dts.add(dtTemp);
        }
        return dts;
    }


    public static String generateDeleteSql(String excel,String tbName,Sheet sheet,String[] fields,String[] fieldValues) {
        String sheetName = sheet.getSheetName();
        StringBuilder sb = new StringBuilder();
        int fieldNum = fields.length;

        for (int i=0;i<fieldNum;i++) {
            if (i==0) {
                sb.append("\"delete from ")
                        .append(tbName)
                        .append(" where ")
                        .append(fields[i]).append("='").append(fieldValues[i]).append("'");
            } else if (i==fieldNum-1) {
                sb.append(" and ").append(fields[i]).append("='").append(fieldValues[i]).append("';\")");
            } else {
                sb.append(" and ").append(fields[i]).append("='").append(fieldValues[i]).append("'");
            }
        }
        String delete = sb.toString();
//        System.out.println(delete);

        /*String deleteSql = "insert into fake_data_log_yuhs(add_time,file_name,sheet_name,delete_sql) values ("+
                "'"+LocalDate.now().toString() +"'," +
                "'"+excel+"',"+
                "'"+sheetName+"',"+
                "\"delete from "+tbName+" where " +
                "cal_time='" +cal_time+"'"+
                "and catalog_id='" +catalog_id+"'"+
                "and tenantId='" +tenantId+"'"+
                "and appkey='" +appkey+"'"+
                "and mediaId='"+mediaId+"';\""+
                ");";*/

        String deleteSql = "insert into fake_data_log_yuhs(add_time,file_name,sheet_name,delete_sql) values ("+
                "'"+LocalDate.now().toString() +"'," +
                "'"+excel+"',"+
                "'"+sheetName+"',"+
                delete +
                ";";
        return deleteSql;
    }

}
