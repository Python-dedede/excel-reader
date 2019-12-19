package cn.me.excel;

import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * @Classname ExcelReader2
 * @Description
 * @Date 2019/12/18 9:49
 * @Created by yuhousheng
 */
public class ExcelReader2 {

    private static Connection con = null;
    private static Statement st = null;
    private static String path2Excels = "";
    private static String path2ExcelsPrefix = "";

    static {
        try {
            // mysql相关初始化
            InputStream is = ExcelReader2.class.getClassLoader().getResourceAsStream("db.properties");
            Properties pp = new Properties();
            pp.load(is);

            String username = pp.getProperty("username");
            String password = pp.getProperty("password");
            String url = pp.getProperty("url");

            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(url, username, password);
            st = con.createStatement();

            // 待入库的文件集合
            InputStream is2 = ExcelReader2.class.getClassLoader().getResourceAsStream("common.properties");
            Properties pp2 = new Properties();
            pp2.load(is2);
            path2Excels = pp2.getProperty("path2Excels");
            path2ExcelsPrefix = pp2.getProperty("path2ExcelsPrefix");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        // 获取待入库的excel文件
        List<String> excelFiles = CommonUtils.getExcelFiles(path2Excels, path2ExcelsPrefix);

        // 进行excel入库
        for (String excel : excelFiles) {
            String excelName = excel.split("mysql\\\\")[1]; // excel文件名称
            FileInputStream is = new FileInputStream(excel);
            Workbook workbook = StreamingReader.builder()
                    .rowCacheSize(100)
                    .bufferSize(1024 * 1024 * 500)
                    .open(is);

            //租户id表
            Sheet tenantSheet = workbook.getSheet("租户id表");
            List<List<String>> tenantRows = CommonUtils.getCellValue(tenantSheet, 1);
            String tenantId = tenantRows.get(0).get(0);


            //################################## 开始进行入库操作 ################################/
            //7天阅读量排行的稿件top
            Sheet topArticleReadSheet = workbook.getSheet("7天阅读量排行的稿件top");
            topArticleRead2Mysql(excelName, topArticleReadSheet, st, 2, tenantId); // 0文章标题，1文章阅读量  excel sheet中的列位置 0：代表第一列，。。。。


            //7天阅读量排行栏目TOP
            Sheet topCatalogReadSheet = workbook.getSheet("7天阅读量排行栏目TOP");
            topCatalogRead2Mysql(excelName, topCatalogReadSheet, st, 2, tenantId);// 0栏目名称，1阅读量


            //注册会员地域分布
            Sheet registerRegionDistributeSheet = workbook.getSheet("注册会员地域分布");
            registerRegionDistribute2Mysql(excelName, registerRegionDistributeSheet, st, 2, tenantId);// 0地市名称，1注册会员数

            //激活趋势，阅读量趋势，注册数据趋势
            Sheet trendSheet = workbook.getSheet("激活趋势，阅读量趋势，注册数据趋势");
            activate_read_register_trend2Mysql(excelName, trendSheet, st, 4, tenantId);// 0日期，1激活量，2阅读量，3注册量


            //24小时阅读量时间段分布
            Sheet latest24ReadSheet = workbook.getSheet("24小时阅读量时间段分布");
            latest24Read2Mysql(excelName, latest24ReadSheet, st, 2, tenantId);// 0日期，1阅读量


            //7天转发分享数排行的文章TOP
            Sheet topArticleShareSheet = workbook.getSheet("7天转发分享数排行的文章TOP");
            topArticleShare2Mysql(excelName, topArticleShareSheet, st, 2, tenantId);// 0文章名称，1分享数


            //中间位置注册数
            Sheet centerRegisterSheet = workbook.getSheet("中间位置注册数");
            centerRegister2Mysql(excelName, centerRegisterSheet, st, 2, tenantId);// 0平台，1注册量


            //中间位置阅读量，激活数
            Sheet centerReadActivateSheet = workbook.getSheet("中间位置阅读量，激活数");
            centerReadActivate2Mysql(excelName, centerReadActivateSheet, st, 2, tenantId);// 0阅读量，1激活量


            //中间位置日阅读量+日活跃用户数
            Sheet centerDayPvUvSheet = workbook.getSheet("中间位置日阅读量+日活跃用户数");
            centerDayPvUv2Mysql(excelName, centerDayPvUvSheet, st, 2, tenantId);// 0日阅读量，1日活跃用户数


            //中间位置日均停留时长
            Sheet centerDayStaySheet = workbook.getSheet("中间位置日均停留时长");
            centerDayStay2Mysql(excelName, centerDayStaySheet, st, 2, tenantId);//0访问时长，1访问次数


            //中间位置的评论，点赞，分享
            Sheet centerCommentLikeShareSheet = workbook.getSheet("中间位置的评论，点赞，分享");
            centerCommentLikeShare2Mysql(excelName, centerCommentLikeShareSheet, st, 3, tenantId);//0评论，1点赞，2分享


            //释放资源
            is.close();
            workbook.close();
            st.close();
        }

    }

    private static void centerCommentLikeShare2Mysql(String excel, Sheet centerCommentLikeShareSheet, Statement st, int i, String tenantId) throws SQLException {
        System.out.println("\n\n\n\n\n\n-----------------------------正在执行#" + centerCommentLikeShareSheet.getSheetName());
        List<List<String>> centerCommentLikeShareRows = CommonUtils.getCellValue(centerCommentLikeShareSheet, 3);
        // 不受时间限制的指标
        //`cal_time`,`catalog_id`,`tenantid`,`appkey`,`mediaId`,`comment_cur`,`like_cur`,`share_cur`
        String dt = LocalDate.now().toString();
        for (List<String> r : centerCommentLikeShareRows) {
            String cal_time = dt;
            String catalog_id = UUID.randomUUID().toString().substring(0, 2);
            String tenantId2 = tenantId;
            String appkey = UUID.randomUUID().toString().substring(0, 9);
            String mediaId = UUID.randomUUID().toString().substring(0, 9);
            String comment_cur = r.get(0);
            String like_cur = r.get(1);
            String share_cur = r.get(2);
            String centerCommentLikeShareSql = "insert into app_catalog_d(`cal_time`,`catalog_id`,`tenantid`,`appkey`,`mediaId`,`comment_cur`,`like_cur`,`share_cur`) values(" +
                    "'" + cal_time + "'," +
                    "'" + catalog_id + "'," +
                    "'" + tenantId2 + "'," +
                    "'" + appkey + "'," +
                    "'" + mediaId + "'," +
                    comment_cur + "," +
                    like_cur + "," +
                    share_cur +
                    ");";
            String s = CommonUtils.generateDeleteSql(
                    excel
                    , "app_catalog_d"
                    , centerCommentLikeShareSheet
                    , new String[]{"cal_time", "catalog_id", "tenantId", "appkey", "mediaId"}
                    , new String[]{cal_time, catalog_id, tenantId, appkey, mediaId});
//            System.out.println("------commonUtils:"+s);
            st.addBatch(s);
//            System.out.println(centerCommentLikeShareSql);
            st.addBatch(centerCommentLikeShareSql);
        }
        st.executeBatch();
    }


    private static void centerDayStay2Mysql(String excel, Sheet centerDayStaySheet, Statement st, int i, String tenantId) throws SQLException {
        System.out.println("\n\n\n\n\n\n-----------------------------正在执行#" + centerDayStaySheet.getSheetName());
        List<List<String>> centerDayStayRows = CommonUtils.getCellValue(centerDayStaySheet, 2);
        List<String> latest60Dts = CommonUtils.get60DateStrs();

        for (String dt : latest60Dts) {
            //`cal_time`,`prov_name`,`tenantid`,`appkey`,`dur_cur`,`session_cnt`
            for (List<String> r : centerDayStayRows) {
                String cal_time = dt;
                String prov_name = UUID.randomUUID().toString().substring(0, 2);
                String tenantId2 = tenantId;
                String appkey = UUID.randomUUID().toString().substring(0, 9);
                String dur_cur = r.get(0);
                String session_cnt = r.get(1);
                String centerDayStaySsql = "insert into app_user_cnt_d(`cal_time`,`prov_name`,`tenantid`,`appkey`,`dur_cur`,`session_cnt`) values (" +
                        "'" + cal_time + "'," +
                        "'" + prov_name + "'," +
                        "'" + tenantId2 + "'," +
                        "'" + appkey + "'," +
                        dur_cur + "," +
                        session_cnt +
                        ");";
                String s = CommonUtils.generateDeleteSql(
                        excel
                        , "app_user_cnt_d"
                        , centerDayStaySheet
                        , new String[]{"cal_time", "prov_name", "tenantid", "appkey"}
                        , new String[]{cal_time, prov_name, tenantId, appkey});
//            System.out.println("------commonUtils:"+s);
                st.addBatch(s);
//                System.out.println(centerDayStaySsql);
                st.addBatch(centerDayStaySsql);
            }
        }

        st.executeBatch();

    }

    private static void centerDayPvUv2Mysql(String excel, Sheet centerDayPvUvSheet, Statement st, int i, String tenantId) throws SQLException {
        System.out.println("\n\n\n\n\n\n-----------------------------正在执行#" + centerDayPvUvSheet.getSheetName());
        List<List<String>> centerDayPvUvRows = CommonUtils.getCellValue(centerDayPvUvSheet, 2);
        List<String> latest60Dts = CommonUtils.get60DateStrs();

        for (String dt : latest60Dts) {
            //`cal_time`,`prov_name`,`tenantid`,`appkey`,`pv_cur`,`uv_cur`
            for (List<String> r : centerDayPvUvRows) {
                String cal_time = dt;
                String prov_name = UUID.randomUUID().toString().substring(0, 2);
                String tenantId2 = tenantId;
                String appkey = UUID.randomUUID().toString().substring(0, 9);
                String pv_cur = r.get(0);
                String uv_Cur = r.get(1);
                String centerDayPvUvSql = "insert into app_user_cnt_d(`cal_time`,`prov_name`,`tenantid`,`appkey`,`pv_cur`,`uv_cur`) values (" +
                        "'" + cal_time + "'," +
                        "'" + prov_name + "'," +
                        "'" + tenantId2 + "'," +
                        "'" + appkey + "'," +
                        pv_cur + "," +
                        uv_Cur +
                        ");";
                String s = CommonUtils.generateDeleteSql(
                        excel
                        , "app_user_cnt_d"
                        , centerDayPvUvSheet
                        , new String[]{"cal_time", "prov_name", "tenantid", "appkey"}
                        , new String[]{cal_time, prov_name, tenantId, appkey});
//            System.out.println("------commonUtils:"+s);
                st.addBatch(s);
//                System.out.println(centerDayPvUvSql);
                st.addBatch(centerDayPvUvSql);
            }
        }
        st.executeBatch();
    }

    private static void centerReadActivate2Mysql(String excel, Sheet centerReadActivateSheet, Statement st, int i, String tenantId) throws SQLException {
        System.out.println("\n\n\n\n\n\n-----------------------------正在执行#" + centerReadActivateSheet.getSheetName());
        List<List<String>> centerReadActivateRows = CommonUtils.getCellValue(centerReadActivateSheet, 2);
        // 不受时间限制的指标
        //`cal_time`,`prov_name`,`tenantid`,`appkey`,`pv_cur`,`activate_cur`
        String dt = LocalDate.now().toString();
        for (List<String> r : centerReadActivateRows) {
            String cal_time = dt;
            String prov_name = UUID.randomUUID().toString().substring(0, 2);
            String tenantId2 = tenantId;
            String appkey = UUID.randomUUID().toString().substring(0, 9);
            String pv_cur = r.get(0);
            String activate_cur = r.get(1);
            String centerReadActivateSql = "insert into app_user_cnt_d(`cal_time`,`prov_name`,`tenantid`,`appkey`,`pv_cur`,`activate_cur`) values(" +
                    "'" + cal_time + "'," +
                    "'" + prov_name + "'," +
                    "'" + tenantId2 + "'," +
                    "'" + appkey + "'," +
                    pv_cur + "," +
                    activate_cur +
                    ");";
            String s = CommonUtils.generateDeleteSql(
                    excel
                    , "app_user_cnt_d"
                    , centerReadActivateSheet
                    , new String[]{"cal_time", "prov_name", "tenantid", "appkey"}
                    , new String[]{cal_time, prov_name, tenantId, appkey});
//            System.out.println("------commonUtils:"+s);
            st.addBatch(s);
//            System.out.println(centerReadActivateSql);
            st.addBatch(centerReadActivateSql);
        }
        st.executeBatch();


    }

    private static void centerRegister2Mysql(String excel, Sheet centerRegisterSheet, Statement st, int i, String tenantId) throws SQLException {
        System.out.println("\n\n\n\n\n\n-----------------------------正在执行#" + centerRegisterSheet.getSheetName());
        // 不受时间限制的指标
        //`cal_time`,`prov_name`,`tenantid`,`appkey`,`source_flag`,`registered_cur`
        List<List<String>> centerRegisterRows = CommonUtils.getCellValue(centerRegisterSheet, 2);
        String dt = LocalDate.now().toString();
        for (List<String> r : centerRegisterRows) {
            String cal_time = dt;
            String prov_name = UUID.randomUUID().toString().substring(0, 2);
            String tenantId2 = tenantId;
            String appkey = UUID.randomUUID().toString().substring(0, 9);
            String source_flag = r.get(0);
            String registered_cur = r.get(1);
            String centerRegisterSql = "insert into app_user_cnt_d(`cal_time`,`prov_name`,`tenantid`,`appkey`,`source_flag`,`registered_cur`) values(" +
                    "'" + cal_time + "'," +
                    "'" + prov_name + "'," +
                    "'" + tenantId2 + "'," +
                    "'" + appkey + "'," +
                    source_flag + "," +
                    registered_cur +
                    ");";
            String s = CommonUtils.generateDeleteSql(
                    excel
                    , "app_user_cnt_d"
                    , centerRegisterSheet
                    , new String[]{"cal_time", "prov_name", "tenantid", "appkey"}
                    , new String[]{cal_time, prov_name, tenantId, appkey});
//            System.out.println("------commonUtils:"+s);
            st.addBatch(s);
//            System.out.println(centerRegisterSql);
            st.addBatch(centerRegisterSql);
        }
        st.executeBatch();

    }

    private static void topArticleShare2Mysql(String excel, Sheet topArticleShareSheet, Statement st, int i, String tenantId) throws SQLException {
        System.out.println("\n\n\n\n\n\n-----------------------------正在执行#" + topArticleShareSheet.getSheetName());
        List<List<String>> topArticleShareRows = CommonUtils.getCellValue(topArticleShareSheet, i);

        // 时间列表
        List<String> dateList = generateRecentDateStr(10);

        // 开始插数据
        for (String dt : dateList) {
            for (List<String> row : topArticleShareRows) {
                String cal_time = dt;
                String id = UUID.randomUUID().toString().substring(0, 9);
                String catalog_id = UUID.randomUUID().toString().substring(0, 9);
                String tenantId2 = tenantId;
                String appkey = UUID.randomUUID().toString().substring(0, 9);
                String title = row.get(0); //文章标题
                String share_cur = row.get(1); //分享量
//                System.out.println("title:"+title + ",pv:"+readCnt);
                String topArticleShareSql = "insert into app_article_d" + // PRIMARY KEY (`id`,`cal_time`,`catalog_id`,`tenantid`,`appkey`)
                        "(cal_time,id,catalog_id,share_cur,tenantid,appkey,title) " +
                        "values(" +
                        "'" + cal_time + "'," +
                        "'" + id + "'," +
                        "'" + catalog_id + "'," +
                        share_cur + "," + //分享数
                        "'" + tenantId2 + "'," +
                        "'" + appkey + "'," +
                        "'" + title + "'" +
                        ");";
                String s = CommonUtils.generateDeleteSql(
                        excel
                        , "app_article_d"
                        , topArticleShareSheet
                        , new String[]{"id", "cal_time", "catalog_id", "tenantid", "appkey"}
                        , new String[]{id, cal_time, catalog_id, tenantId, appkey});
//            System.out.println("------commonUtils:"+s);
                st.addBatch(s);
//                System.out.println(topArticleShareSql);
                st.addBatch(topArticleShareSql);
            }
        }
        st.executeBatch();

    }

    private static void latest24Read2Mysql(String excel, Sheet latest24ReadSheet, Statement st, int i, String tenantId) throws SQLException {
        System.out.println("\n\n\n\n\n\n-----------------------------正在执行#" + latest24ReadSheet.getSheetName());
        List<List<String>> latest24ReadRows = CommonUtils.getCellValue(latest24ReadSheet, i);
        int rowNo = 1;
        for (List<String> r : latest24ReadRows) {// `cal_time`,`hour_str`,`id`,`catalog_id`,`tenantid`,`appkey`,`pv_cur`
            rowNo += 1;
            String dt = r.get(0);
            // 判断日期是否是整点,例如: 2019-12-18 17:00:00，可能会出现2019-12-18 16:59:59这种格式的日期
            boolean notIntegrateHour = dt.contains("59");
            if (notIntegrateHour) {
                throw new IllegalArgumentException(rowNo + "行的数据中小时有非整点的，请检查#24小时阅读量时间段分布#中的日期，并且请规范填写");
            }
            String cal_time = dt.substring(0, 10);
            String hour_str = dt.substring(11, 13);
            String id = UUID.randomUUID().toString().substring(0, 9);
            String catalog_id = UUID.randomUUID().toString().substring(0, 9);
            String tenantId2 = tenantId;
            String appkey = UUID.randomUUID().toString().substring(0, 9);
            String pv_cur = r.get(1);
            String latest24ReadSql = "insert into app_article_h(`cal_time`,`hour_str`,`id`,`catalog_id`,`tenantid`,`appkey`,`pv_cur`) values(" +
                    "'" + cal_time + "'," +
                    "'" + hour_str + "'," +
                    "'" + id + "'," +
                    "'" + catalog_id + "'," +
                    "'" + tenantId2 + "'," +
                    "'" + appkey + "'," +
                    pv_cur +
                    ");";
            String s = CommonUtils.generateDeleteSql(
                    excel
                    , "app_article_h"
                    , latest24ReadSheet
                    , new String[]{"id", "cal_time", "catalog_id", "tenantid", "appkey", "hour_str"}
                    , new String[]{id, cal_time, catalog_id, tenantId, appkey, hour_str});
//            System.out.println("------commonUtils:"+s);
            st.addBatch(s);
//            System.out.println(latest24ReadSql);
            st.addBatch(latest24ReadSql);
        }
        st.executeBatch();

    }

    private static void activate_read_register_trend2Mysql(String excel, Sheet trendSheet, Statement st, int collumnNum, String tenantId) throws SQLException {
        System.out.println("\n\n\n\n\n\n-----------------------------正在执行#" + trendSheet.getSheetName());
        List<List<String>> trendRows = CommonUtils.getCellValue(trendSheet, collumnNum);
        for (List<String> r : trendRows) { //`cal_time`,`hour_str`,`prov_name`,`tenantid`,`appkey`,`activate_cur`,`pv_cur`,`registered_cur`
            // 0日期，1激活量，2阅读量，3注册量
            String cal_time = r.get(0);
            String hour_str = "00";
            String prov_name = UUID.randomUUID().toString().substring(0, 2);
            String tenantId2 = tenantId;
            String appkey = UUID.randomUUID().toString().substring(0, 9);
            String activate_cur = r.get(1);
            String pv_cur = r.get(2);
            String registered_cur = r.get(3);
            String trendSql = "insert into app_user_cnt_h(`cal_time`,`hour_str`,`prov_name`,`tenantid`,`appkey`,`activate_cur`,`pv_cur`,`registered_cur`) values(" +
                    "'" + cal_time + "'," +
                    "'" + hour_str + "'," +
                    "'" + prov_name + "'," +
                    "'" + tenantId2 + "'," +
                    "'" + appkey + "'," +
                    activate_cur + "," +
                    pv_cur + "," +
                    registered_cur +
                    ");";
            String s = CommonUtils.generateDeleteSql(
                    excel
                    , "app_user_cnt_h"
                    , trendSheet
                    , new String[]{"cal_time", "prov_name", "tenantid", "appkey", "hour_str"}
                    , new String[]{cal_time, prov_name, tenantId, appkey, hour_str});
//            System.out.println("------commonUtils:"+s);
            st.addBatch(s);
//            System.out.println(trendSql);
            st.addBatch(trendSql);
        }
        st.executeBatch();

    }

    private static void registerRegionDistribute2Mysql(String excel, Sheet registerRegionDistributeSheet, Statement st, int collumnNum, String tenantId) throws SQLException {
        System.out.println("\n\n\n\n\n\n-----------------------------正在执行#" + registerRegionDistributeSheet.getSheetName());
        List<List<String>> reRegionDistributeRows = CommonUtils.getCellValue(registerRegionDistributeSheet, collumnNum);
        // 由于对于时间的限制是从建站开始到当天日期，所以这里的时间就用当天的就行，而且这个数据只需要入一次就能永久使用
        LocalDate now = LocalDate.now();
        String dt = now.toString();

        for (List<String> row : reRegionDistributeRows) {
            String cal_time = dt;
            String city = row.get(0);
            String registered_cur = row.get(1);
            String tenantId2 = tenantId;
            String appkey = UUID.randomUUID().toString().substring(0, 9);
            String reRegionDistributeSql = "insert into app_city_register_d(cal_time,city,registered_cur,tenantid,appkey) values(" +
                    "'" + cal_time + "'," +
                    "'" + city + "'," +
                    registered_cur + "," +
                    "'" + tenantId2 + "'," +
                    "'" + appkey + "'" +
                    ");";
            String s = CommonUtils.generateDeleteSql(
                    excel
                    , "app_city_register_d"
                    , registerRegionDistributeSheet
                    , new String[]{"cal_time", "city", "tenantid", "appkey"}
                    , new String[]{cal_time, city, tenantId, appkey});
//            System.out.println("------commonUtils:"+s);
            st.addBatch(s);
//            System.out.println(reRegionDistributeSql);
            st.addBatch(reRegionDistributeSql);
        }
        st.executeBatch();

    }

    private static void topCatalogRead2Mysql(String excel, Sheet topCatalogReadSheet, Statement st, int collumnNum, String tenantId) throws SQLException {
        System.out.println("\n\n\n\n\n\n-----------------------------正在执行#" + topCatalogReadSheet.getSheetName());
        List<List<String>> topCatalogReadRows = CommonUtils.getCellValue(topCatalogReadSheet, collumnNum);
        List<String> dateList = generateRecentDateStr(10);

        for (String dt : dateList) { //PRIMARY KEY (`cal_time`,`catalog_id`,`tenantid`,`appkey`,`mediaId`)
            for (List<String> row : topCatalogReadRows) {
                String cal_time = dt;
                String catalog_id = UUID.randomUUID().toString().substring(0, 9);
                String tenantId2 = tenantId;
                String appkey = UUID.randomUUID().toString().substring(0, 9);
                String mediaId = UUID.randomUUID().toString().substring(0, 9);
                String catalog_name = row.get(0);
                String readCnt = row.get(1);

                String topCatalogReadSql = "insert into app_catalog_d(`cal_time`,`catalog_id`,`tenantid`,`appkey`,`mediaId`,`catalog_name`,`pv_cur`) values(" +
                        "'" + cal_time + "'," +
                        "'" + catalog_id + "'," +
                        "'" + tenantId2 + "'," +
                        "'" + appkey + "'," +
                        "'" + mediaId + "'," +
                        "'" + catalog_name + "'," +
                        readCnt + "" +
                        ");";
                String s = CommonUtils.generateDeleteSql(
                        excel
                        , "app_catalog_d"
                        , topCatalogReadSheet
                        , new String[]{"cal_time", "catalog_id", "tenantid", "appkey", "mediaId"}
                        , new String[]{cal_time, catalog_id, tenantId, appkey, mediaId});
//            System.out.println("------commonUtils:"+s);
                st.addBatch(s);
//                System.out.println(topCatalogReadSql);
                st.addBatch(topCatalogReadSql);
            }
        }
        st.executeBatch();

    }


    /**
     * 7天稿件阅读量排行top
     *
     * @param sheet
     * @param st
     * @param collumnNum 这个sheet页的行数据有多少列
     * @param tenantId
     */
    private static void topArticleRead2Mysql(String excel, Sheet sheet, Statement st, int collumnNum, String tenantId) throws SQLException {
        System.out.println("\n\n\n\n\n\n-----------------------------正在执行#" + sheet.getSheetName());
        List<List<String>> topArticleReadRows = CommonUtils.getCellValue(sheet, collumnNum);

        // 时间列表
        List<String> dateList = generateRecentDateStr(10);

        // 开始插数据
        for (String dt : dateList) {
            for (List<String> row : topArticleReadRows) {
                String cal_time = dt;
                String id = UUID.randomUUID().toString().substring(0, 9);
                String catalog_id = UUID.randomUUID().toString().substring(0, 9);
                String tenantId2 = tenantId;
                String appkey = UUID.randomUUID().toString().substring(0, 9);
                String title = row.get(0); //文章标题
                String readCnt = row.get(1); //阅读量
//                System.out.println("title:"+title + ",pv:"+readCnt);
                String topArticleReadSql = "insert into app_article_d" + // PRIMARY KEY (`id`,`cal_time`,`catalog_id`,`tenantid`,`appkey`)
                        "(cal_time,id,catalog_id,pv_cur,tenantid,appkey,title) " +
                        "values(" +
                        "'" + cal_time + "'," +
                        "'" + id + "'," +
                        "'" + catalog_id + "'," +
                        readCnt + "," + //pv_cur
                        "'" + tenantId2 + "'," +
                        "'" + appkey + "'," +
                        "'" + title + "'" +
                        ");";
                String s = CommonUtils.generateDeleteSql(
                        excel
                        , "app_article_d"
                        , sheet
                        , new String[]{"cal_time", "id", "catalog_id", "tenantid", "appkey", "title"}
                        , new String[]{cal_time, id, catalog_id, tenantId, appkey, title});
//            System.out.println("------commonUtils:"+s);
                st.addBatch(s);
//                System.out.println(topArticleReadSql);
                st.addBatch(topArticleReadSql);
            }
        }
        st.executeBatch();

    }


    /**
     * 功能： 自动产生最近几轮的日期列表 例如：今天是2019-12-18 那么[2019-12-10, 2019-12-18, 2019-12-26, .....]
     * 为什么是8天为一轮呢，因为是多是展示最近7天的数据
     *
     * @return 日期列表
     */
    public static List<String> generateRecentDateStr(int rounds) {
        // 按照当前时间往前推一轮，往后推10轮
        List<String> dateList = new ArrayList<String>();

        // 构造时间
        LocalDate now = LocalDate.now();
        String pre1 = CommonUtils.getDateStr(now, -8L); //时间往前推一轮的目的：可以让客户立马见到数据效果，而不必等到展示日期才可以看到
        dateList.add(pre1);
        dateList.add(now.toString());
        for (int i = 1; i <= rounds; i++) {
            Long aLong = Long.valueOf(String.valueOf(i * 8));
            String dtTemp = CommonUtils.getDateStr(now, aLong);
            dateList.add(dtTemp);
        }
        System.out.println("自动生成的日期： " + dateList);
        return dateList;
    }
}
