package cn.me.excel;

import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @Classname MyExcelReader
 * @Description
 *    读取excel文件内容，实现插入mysql表
 * @Date 2019/9/6 11:31
 * @Created by yuhousheng
 */
public class MyExcelReader {

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e ) {
            System.out.println("加载驱动出错");
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
            throws IOException, SQLException {
        Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3308/haihe_insight?characterEncoding=utf8&serverTimezone=UTC"
                , "haihe2"
                , "Newmeds0bey");
        Statement st = con.createStatement();

        BufferedReader br = new BufferedReader(new FileReader("c:\\users\\31883\\desktop\\长江云数据\\files.txt"));
        String line="";
        while ((line=br.readLine())!=null) {
            if (line.startsWith("#")) {
                System.out.println("已经注释掉的文件: "+line);
                continue;
            }
            String name = "c:\\users\\31883\\desktop\\长江云数据\\" + line; //文件名称
            System.out.println("正在读取的文件是: "+name);
            // 读取excel文件
            FileInputStream is = new FileInputStream(name);
            Workbook workbook = StreamingReader.builder()
                    .rowCacheSize(100)
                    .bufferSize(1024 * 1024 * 500)
                    .open(is);

            // 开始遍历excel sheet页内容，并往对应的mysql表插入
            try {
                Sheet catalog = workbook.getSheet("catalog表");
                catalog(st, catalog);

                Sheet readHourly = workbook.getSheet("30天稿件阅读量趋势+7天稿件top+中间位置+7天栏目");
                readHourly(st, readHourly);
                System.out.println("-----> 30天稿件阅读量趋势+7天稿件top+中间位置+7天栏目");

                Sheet memberRegisterHourly = workbook.getSheet("30天会员注册数趋势+中间展示位置相关指标");
                memberRegisterHourly(st, memberRegisterHourly);
                System.out.println("-----> 30天会员注册数趋势+中间展示位置相关指标");

                Sheet appInstallHourly = workbook.getSheet("30天APP激活数趋势+累计激活数app_install_ho");
                appInstallHourly(st, appInstallHourly);
                System.out.println("-----> 30天APP激活数趋势+累计激活数app_install_ho");

                Sheet memberRegionHourly = workbook.getSheet("注册会员地域分布排行member_region_hourly");
                memberRegionHourly(st, memberRegionHourly);
                System.out.println("-----> 注册会员地域分布排行member_region_hourly");

                Sheet activeUserDaily = workbook.getSheet("日活跃用户数active_user_daily");
                activeUserDaily(st, activeUserDaily);
                System.out.println("-----> 日活跃用户数active_user_daily");

                Sheet commentHourly = workbook.getSheet("评论数comment_hourly");
                commentHourly(st, commentHourly);
                System.out.println("-----> 评论数comment_hourly");

                Sheet forwardHourly = workbook.getSheet("7天转发分享数+转发数forward_hourly");
                forwardHourly(st, forwardHourly);
                System.out.println("-----> 7天转发分享数+转发数forward_hourly");

                Sheet praiseHourly = workbook.getSheet("点赞数praise_hourly");
                praiseHourly(st, praiseHourly);
                System.out.println("-----> 点赞数praise_hourly");

                Sheet appStartHourly = workbook.getSheet("启动次数app_start_hourly");
                appStartHourly(st, appStartHourly);
                System.out.println("-----> 启动次数app_start_hourly");

                Sheet stayDaily = workbook.getSheet("日均停留时长stay_daily");
                stayDaily(st, stayDaily);
                System.out.println("-----> 日均停留时长stay_daily");
                System.out.println("------------------------------------- end \n\n\n");
            } catch (NullPointerException npe) {
                System.out.println("excel中有空白行或者有空白的cell，因此引起了npe");
                npe.printStackTrace();
            }
        }
        con.close();
        st.close();

    }


    /**
     *  catalog
     */
    private static void catalog(Statement st,Sheet sheet) throws SQLException {
        // 遍历sheet中的数据，生成相关sql，利用st.addBatch(sql)，加入所有sql
        for (Row row : sheet) { // 行
            if (null != row && 0 == row.getRowNum()) { // 跳过第一行的标题
                continue;
            }
            String catalogId = row.getCell(0).getStringCellValue(); // 栏目id
            String catalogName = row.getCell(1).getStringCellValue(); // catalogName
            String parentId = row.getCell(2).getStringCellValue(); // 0
            String treeLevel = row.getCell(3).getStringCellValue(); // 5
            String type = row.getCell(4).getStringCellValue(); // '5'
            String isleaf = row.getCell(5).getStringCellValue(); // 0
            String publishFlag = row.getCell(6).getStringCellValue(); // 'Y'
            String rootId = row.getCell(7).getStringCellValue();
            String siteId= row.getCell(8).getStringCellValue(); // 1
            String tenantId = row.getCell(9).getStringCellValue();
            // 添加批处理sql
            String sql =
                    "insert into catalog(catalogId,catalogName,parentId,treelevel,`type`,`isLeaf`,publishFlag,rootId,siteId,tenantId) "
                            + "values("
                            + (catalogId.length()==0?-999:catalogId)
                            + ",'" + catalogName + "'"
                            + "," + 0 + "" // parentId
                            + "," + 5 + ""  //treeLevel
                            + ",'" + 5 + "'" // type
                            + "," + 0 + "" // isleaf
                            + ",'Y'" // publishFlag
                            + "," + (rootId.length()==0?-999:rootId)
                            + ",'1'" // siteId
                            + ",'"+tenantId+"'"
                            + ") on duplicate key update "
                            + "catalogName=" + "'" + catalogName + "'"
                            + ",parentId=0"
                            + ",treeLevel=5"
                            + ",type='5'"
                            + ",isleaf=0"
                            + ",publishFlag='Y'"
                            + ",rootId=" + (rootId.length()==0?-999:rootId)
                            + ",siteId='1'" ;
//            System.out.println(sql);
            st.addBatch(sql);
        }
        st.executeBatch();
    }


    /**
     * read_hourly
     */
    private static void readHourly(Statement st ,Sheet sheet ) throws SQLException {
        // 遍历sheet中的数据，生成相关sql，利用st.addBatch(sql)，加入所有sql
        for (Row row : sheet) { // 行
            if (null != row && 0 == row.getRowNum()) { // 跳过第一行的标题
                continue;
            }
            String title = row.getCell(0).getStringCellValue(); // 文章标题
            String catalogId = row.getCell(1).getStringCellValue(); // 栏目id
            String type = row.getCell(2).getStringCellValue(); // type
            String articleId = row.getCell(3).getStringCellValue(); // 文章id
            String time = row.getCell(4).getStringCellValue(); // 时间yyyy-MM-dd HH:mm:ss
            String read = row.getCell(5).getStringCellValue(); // 阅读量
            String tenantId = row.getCell(6).getStringCellValue(); // 租户id
            String fromDt = row.getCell(7).getStringCellValue(); // 日期yyyy-MM-dd
//            String fixed = row.getCell(8).getStringCellValue(); // fixed
//            System.out.println("==== 插入内容为:title:"+title+",catalogId:"+catalogId+",type:"+type+",articelId:"+articleId +",time:"+time+",read:"+read+",tenantId:"+tenantId+",fromDt:"+fromDt+",fixed:"+fixed);

            // 添加批处理sql
            String sql =
                    "insert into read_hourly(title,catalog,type,articleId,`time`,`count`,tenantId,from_dt,fixed) "
                            + "values("
                            + "'" + title + "'"
                            + ",'" + catalogId + "'"
                            + ",'" + type + "'"
                            + ",'" + articleId + "'"
                            + ",'" + time + "'"
                            + "," + (read.length()==0?0:read)
                            + ",'" + tenantId + "'"
                            + ",'" + fromDt + "'"
                            + "," + 0
                            + ") on duplicate key update "
                            + "title=" + "'" + title + "'"
                            + ",catalog=" + "'" + catalogId + "'"
                            + ",type=''"
                            + ",fixed=8"
                            + ",`count`="+(read.length()==0?0:read)
            ;
//            System.out.println(sql);
            st.addBatch(sql);
        }
        st.executeBatch();
    }


    /**
     * member_register_hourly  30天会员注册数趋势+中间展示位置相关指标
     */
    private static void memberRegisterHourly(Statement st,Sheet sheet) throws SQLException {
        // 遍历sheet中的数据，生成相关sql，利用st.addBatch(sql)，加入所有sql
        for (Row row : sheet) { // 行
            if (null != row && 0 == row.getRowNum()) { // 跳过第一行的标题
                continue;
            }
            String time = row.getCell(0).getStringCellValue(); // 时间yyyy-MM-dd HH:mm:ss
            String tenantId = row.getCell(1).getStringCellValue(); // 租户id
            String memberCount = row.getCell(2).getStringCellValue(); // 注册会员数
            String fromDt = row.getCell(3).getStringCellValue(); // 日期yyyy-MM-dd
            // fixed
            String platForm = row.getCell(5).getStringCellValue(); // ios/android/pc
            // 添加批处理sql
            String sql =
                    "insert into member_register_hourly(`time`,tenantId,`count`,from_dt,fixed,platform) "
                            + "values("
                            + "'" + time + "'"
                            + ",'" + tenantId + "'"
                            + "," + (memberCount.length()==0?0:memberCount)
                            + ",'" + fromDt + "'"
                            + "," + 0 + "" // fixed
                            + ",'" + platForm + "'"
                            + ") on duplicate key update "
                            + "`count`=" + (memberCount.length()==0?0:memberCount)
                            + ",`fixed`=0"
                            + ",platform=" + "'" + platForm + "'"
                    ;
//            System.out.println(sql);
            st.addBatch(sql);
        }
        st.executeBatch();
    }


    /**
     * 30天APP激活数趋势+累计激活数app_install_hourl
     */
    private static void appInstallHourly(Statement st,Sheet sheet) throws SQLException {
        // 遍历sheet中的数据，生成相关sql，利用st.addBatch(sql)，加入所有sql
        for (Row row : sheet) { // 行
            if (null != row && 0 == row.getRowNum()) { // 跳过第一行的标题
                continue;
            }
                String time = row.getCell(0).getStringCellValue();
                String platForm = row.getCell(1).getStringCellValue();
                String activateCount = row.getCell(2).getStringCellValue();
                String tenantId = row.getCell(3).getStringCellValue();
                String fromDt = row.getCell(4).getStringCellValue();

            // fixed
            // 添加批处理sql
            String sql =
                    "insert into app_install_hourly(`time`,platform,`count`,tenantId,from_dt,fixed) "
                            + "values("
                            + "'" + time + "'"
                            + ",'" + platForm + "'"
                            + "," + (activateCount.length()==0?0:activateCount)
                            + ",'" + tenantId + "'"
                            + ",'" + fromDt + "'"
                            + "," + 0 + "" // fixed
                            + ") on duplicate key update "
                            + "`count`=" + (activateCount.length()==0?0:activateCount)
                            + ",`fixed`=0"
                            + ",platform=" + "'" + platForm + "'"
                    ;
            st.addBatch(sql);
        }
        st.executeBatch();
    }


    /**
     *注册会员地域分布排行member_region_hourly
     */
    private static void memberRegionHourly(Statement st,Sheet sheet) throws SQLException {
        // 遍历sheet中的数据，生成相关sql，利用st.addBatch(sql)，加入所有sql
        for (Row row : sheet) { // 行
            if (null != row && 0 == row.getRowNum()) { // 跳过第一行的标题
                continue;
            }
                String time = row.getCell(0).getStringCellValue(); // 时间yyyy-MM-dd HH:mm:ss
                String region = row.getCell(1).getStringCellValue();
                String count = row.getCell(2).getStringCellValue();
                String tenantId = row.getCell(3).getStringCellValue(); // 租户id
                String fromDt = row.getCell(4).getStringCellValue(); // 日期yyyy-MM-dd
                // fixed
                // 添加批处理sql
                String sql =
                        "insert into member_region_hourly(`time`,region,`count`,tenantId,from_dt,fixed) "
                                + "values("
                                + "'" + time + "'"
                                + ",'" + region + "'"
                                + "," + (count.length()==0?0:count)
                                + ",'" + tenantId + "'"
                                + ",'" + fromDt + "'"
                                + "," + 0 + "" // fixed
                                + ") on duplicate key update "
                                + "`count`=" + (count.length()==0?0:count)
                                + ",`fixed`=0";
                st.addBatch(sql);
        }
        st.executeBatch();
    }


    /**
     * 日活跃用户数active_user_daily
     */
    private static void activeUserDaily(Statement st,Sheet sheet) throws SQLException {
        // 遍历sheet中的数据，生成相关sql，利用st.addBatch(sql)，加入所有sql
        for (Row row : sheet) { // 行
            if (null != row && 0 == row.getRowNum()) { // 跳过第一行的标题
                continue;
            }
            String time = row.getCell(0).getStringCellValue(); // 时间yyyy-MM-dd HH:mm:ss
            String count = row.getCell(1).getStringCellValue();
            String tenantId = row.getCell(2).getStringCellValue(); // 租户id
            String fromDt = row.getCell(3).getStringCellValue(); // 日期yyyy-MM-dd
            // fixed
            // 添加批处理sql
            String sql =
                    "insert into active_user_daily(`time`,`count`,tenantId,from_dt) "
                            + "values("
                            + "'" + time + "'"
                            + "," + (count.length()==0?0:count)
                            + ",'" + tenantId + "'"
                            + ",'" + fromDt + "'"
                            + ") on duplicate key update "
                            + "`count`=" + (count.length()==0?0:count)
                    ;
            st.addBatch(sql);
        }
        st.executeBatch();
    }


    /**
     * 评论数comment_hourly
     */
    private static void commentHourly(Statement st,Sheet sheet) throws SQLException {
        // 遍历sheet中的数据，生成相关sql，利用st.addBatch(sql)，加入所有sql
        for (Row row : sheet) { // 行
            if (null != row && 0 == row.getRowNum()) { // 跳过第一行的标题
                continue;
            }
            String title = row.getCell(0).getStringCellValue();
            String catalogId = row.getCell(1).getStringCellValue();
            String type = row.getCell(2).getStringCellValue();
            String articleId = row.getCell(3).getStringCellValue();
            String time = row.getCell(4).getStringCellValue();
            String commentCnt = row.getCell(5).getStringCellValue();
            String tenantId = row.getCell(6).getStringCellValue();
            String fromDt = row.getCell(7).getStringCellValue();
//            String fixed = row.getCell(8).getStringCellValue();
            // fixed
            // 添加批处理sql
            String sql =
                    "insert into comment_hourly(title,catalog,type,articleId,`time`,`count`,tenantId,from_dt,fixed) "
                            + "values("
                            + "'" + title + "'"
                            + ",'" + catalogId + "'"
                            + ",'" + type + "'"
                            + ",'" + articleId + "'"
                            + ",'" + time + "'"
                            + "," + (commentCnt.length()==0?0:commentCnt)
                            + ",'" + tenantId + "'"
                            + ",'" + fromDt + "'"
                            + "," + 0
                      + ")on duplicate key update "
                      + "title=" + "'" + title + "'"
                      + ",catalog=" + "'" + catalogId + "'"
                      + ",type=''"
                      + ",`count`=" + (commentCnt.length()==0?0:commentCnt)
                      + ",`fixed`=" + 0
                    ;
            st.addBatch(sql);
        }
        st.executeBatch();
    }


    /**
     * 7天转发分享数+转发数forward_hourly
     */
    private static void forwardHourly(Statement st,Sheet sheet) throws SQLException {
        // 遍历sheet中的数据，生成相关sql，利用st.addBatch(sql)，加入所有sql
        for (Row row : sheet) { // 行
            if (null != row && 0 == row.getRowNum()) { // 跳过第一行的标题
                continue;
            }
            String title = row.getCell(0).getStringCellValue();
            String catalogId = row.getCell(1).getStringCellValue();
            String type = row.getCell(2).getStringCellValue();
            String articleId = row.getCell(3).getStringCellValue();
            String time = row.getCell(4).getStringCellValue();
            String forwardCnt = row.getCell(5).getStringCellValue();
            String tenantId = row.getCell(6).getStringCellValue();
            String fromDt = row.getCell(7).getStringCellValue();
//            String fixed = row.getCell(8).getStringCellValue();
            // fixed
            // 添加批处理sql
            String sql =
                    "insert into forward_hourly(title,catalog,type,articleId,`time`,`count`,tenantId,from_dt,fixed) "
                            + "values("
                            + "'" + title + "'"
                            + ",'" + catalogId + "'"
                            + ",'" + type + "'"
                            + ",'" + articleId + "'"
                            + ",'" + time + "'"
                            + "," + (forwardCnt.length()==0?0:forwardCnt)
                            + ",'" + tenantId + "'"
                            + ",'" + fromDt + "'"
                            + "," + 0
                            + ")on duplicate key update "
                            + "title=" + "'" + title + "'"
                            + ",catalog=" + "'" + catalogId + "'"
                            + ",type=''"
                            + ",`count`=" + (forwardCnt.length()==0?0:forwardCnt)
                            + ",`fixed`=" + 0
                    ;
            st.addBatch(sql);
        }
        st.executeBatch();
    }


    /**
     * 点赞数praise_hourly
     */
    private static void praiseHourly(Statement st,Sheet sheet) throws SQLException {
        // 遍历sheet中的数据，生成相关sql，利用st.addBatch(sql)，加入所有sql
        for (Row row : sheet) { // 行
            if (null != row && 0 == row.getRowNum()) { // 跳过第一行的标题
                continue;
            }
            String time = row.getCell(0).getStringCellValue();
            String praiseCnt = row.getCell(1).getStringCellValue();
            String articleId = row.getCell(2).getStringCellValue();
            String catalogId = row.getCell(3).getStringCellValue();
            String type = row.getCell(4).getStringCellValue();
            String title = row.getCell(5).getStringCellValue();
            String tenantId = row.getCell(6).getStringCellValue();
            String fromDt = row.getCell(7).getStringCellValue();
//            String fixed = row.getCell(8).getStringCellValue();
            // fixed
            // 添加批处理sql
            String sql =
                    "insert into praise_hourly(`time`,`count`,articleId,catalog,`type`,`title`,tenantId,from_dt,fixed) "
                            + "values("
                            + "'" + time + "'"
                            + "," + (praiseCnt.length()==0?0:praiseCnt)
                            + ",'" + type + "'"
                            + ",'" + articleId + "'"
                            + ",''" // type
                            + ",'" + title + "'"
                            + ",'" + tenantId + "'"
                            + ",'" + fromDt + "'"
                            + "," + 0
                            + ")on duplicate key update "
                            + "`count`=" + (praiseCnt.length()==0?0:praiseCnt)
                            + ",catalog=" + "'" + catalogId + "'"
                            + ",type=''"
                            + ",title=" + "'" + title + "'"
                            + ",`fixed`=" + 0
                    ;
            st.addBatch(sql);
        }
        st.executeBatch();
    }


    /**
     * 启动次数app_start_hourly
     */
    private static void appStartHourly(Statement st,Sheet sheet) throws SQLException {
        // 遍历sheet中的数据，生成相关sql，利用st.addBatch(sql)，加入所有sql
        for (Row row : sheet) { // 行
            if (null != row && 0 == row.getRowNum()) { // 跳过第一行的标题
                continue;
            }
            String time = row.getCell(0).getStringCellValue();
            String tenantId = row.getCell(1).getStringCellValue();
            String fromDt = row.getCell(2).getStringCellValue();
//            String fixed = row.getCell(8).getStringCellValue();
            String startCnt = row.getCell(4).getStringCellValue();
            // fixed
            // 添加批处理sql
            String sql =
                    "insert into app_start_hourly(`time`,`tenantId`,from_dt,fixed,`count`) "
                            + "values("
                            + "'" + time + "'"
                            + ",'" + tenantId + "'"
                            + ",'" + fromDt + "'"
                            + "," + 0
                            + "," + (startCnt.length()==0?0:startCnt)
                            + ")on duplicate key update "
                            + "`count`=" + (startCnt.length()==0?0:startCnt)
                            + ",`fixed`=" + 0
                    ;
//            System.out.println(sql);
            st.addBatch(sql);
        }
        st.executeBatch();
    }


    /**
     * 日均停留时长stay_daily
     */
    private static void stayDaily(Statement st , Sheet sheet) throws SQLException {
        // 遍历sheet中的数据，生成相关sql，利用st.addBatch(sql)，加入所有sql
        for (Row row : sheet) { // 行
            if (null != row && 0 == row.getRowNum()) { // 跳过第一行的标题
                continue;
            }
            String time = row.getCell(0).getStringCellValue();
            String useCnt = row.getCell(1).getStringCellValue();
            String visitCnt = row.getCell(2).getStringCellValue();
            String tenantId = row.getCell(3).getStringCellValue();
            // 添加批处理sql
            String sql =
                    "insert into stay_daily(`time`,`count`,`size`,tenantId) "
                            + "values("
                            + "'" + (time.length()==10?time:"2018-09-09") + "'"
                            + "," + (useCnt.length()==0?0:useCnt)
                            + "," + (visitCnt.length()==0?0:visitCnt)
                            + ",'" + tenantId + "'"
                            + ")on duplicate key update "
                            + "`count`=" + (useCnt.length()==0?0:useCnt)
                            + ",`size`=" + (visitCnt.length()==0?0:visitCnt)
                    ;
//            System.out.println(sql);
            st.addBatch(sql);
        }
        st.executeBatch();
    }
}
