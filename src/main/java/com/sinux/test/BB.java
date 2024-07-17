package com.sinux.test;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.sinux.parser.ScistorSQLParser;
import com.sinux.parser.column.ScistorSelectColumn;
import com.sinux.parser.exception.ScistorParserException;
import com.sinux.parser.result.ScistorResult;
import com.sinux.parser.result.ScistorSelectResult;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class BB {
    public static void main(String[] args) throws ScistorParserException, ClassNotFoundException {
        String sql="SELECT  DISTINCT `1516`.COMPADCAC1.objectModelCode,\n" +
                "  `1516`.COMPADCAC.instanceCode,\n" +
                "  BULD.`BULD2` AS `厂房的子项代码`,\n" +
                "  ROOM.`ROOM5` AS `建筑标高`,\n" +
                "  COMPADCAC1.`COMPAD29` AS `安装标准号`,\n" +
                "  COMPADCAC1.`COMP1` AS `位号-机组号`,\n" +
                "  COMPADCAC.`COMP2` AS `位号-系统代码`,\n" +
                "  COMPADCAC.`COMP3` AS `位号-流水号`,\n" +
                "  COMPADCAC.`COMP4` AS `位号-设备类型代码`,\n" +
                "  COMPADCAC.`COMP5` AS `位号-设备部件码`,\n" +
                "  COMPADCAC.`COMP6` AS `设备名称`,\n" +
                "  COMPADCAC.`COMP7` AS `设备类别编码`,\n" +
                "  COMPADCAC.`COMP8` AS `安装房间号`,\n" +
                "  COMPADCAC.`COMP9` AS `采购技术规格书`,\n" +
                "  COMPADCAC.`COMP10` AS `外形尺寸要求`,\n" +
                "  COMPADCAC.`COMP11` AS `最大空载重量`,\n" +
                "  COMPADCAC.`COMP12` AS `最大承载重量`,\n" +
                "  COMPADCAC.`COMP13` AS `设备实际空载重量`,\n" +
                "  COMPADCAC.`COMP14` AS `设备实际承载重量`,\n" +
                "  COMPADCAC.`COMP15` AS `防爆等级要求`,\n" +
                "  COMPADCAC.`COMP16` AS `实际防爆等级`,\n" +
                "  COMPADCAC.`COMP17` AS `辐照要求`,\n" +
                "  COMPADCAC.`COMP18` AS `设计寿命`,\n" +
                "  COMPADCAC.`COMP19` AS `屏障等级`,\n" +
                "  COMPADCAC.`COMP20` AS `功能等级`,\n" +
                "  COMPADCAC.`COMP21` AS `抗震类别`,\n" +
                "  COMPADCAC.`COMP22` AS `鉴定等级`,\n" +
                "  COMPADCAC.`COMP23` AS `质保等级`,\n" +
                "  COMPADCAC.`COMP24` AS `清洁度等级`,\n" +
                "  COMPADCAC.`COMP25` AS `IP防护等级要求`,\n" +
                "  COMPADCAC.`COMP26` AS `设备实际IP防护等级`,\n" +
                "  COMPADCAC.`COMP27` AS `IK防护等级要求`,\n" +
                "  COMPADCAC.`COMP28` AS `设备实际IK防护等级`,\n" +
                "  COMPADCAC.`COMP32` AS `供货商名称`,\n" +
                "  COMPADCAC.`COMPA10` AS `工作`,\n" +
                "  COMPADCAC.`COMPA11` AS `备用情况说明`,\n" +
                "  COMPADCAC.`COMPA13` AS `电源类型`,\n" +
                "  COMPADCAC.`COMPAD3` AS `设计安装方式`,\n" +
                "  COMPADCAC.`COMPAD19` AS `通信接口协议`,\n" +
                "  COMPADCAC.`COMPAD20` AS `通信接口类型`,\n" +
                "  COMPADCAC.`COMPAD25` AS `是否随其他设备供货`,\n" +
                "  COMPADCAC.`COMPAD26` AS `是否自带支架`,\n" +
                "  COMPADCAC.`COMPAD27` AS `是否自带仪控箱盒`,\n" +
                "  COMPADCAC.`COMPAD28` AS `安装图号`,\n" +
                "  COMPADCAC.`COMPAD30` AS `仪表架号`,\n" +
                "  COMPADCAC.`COMPAD31` AS `仪表管道布置图号`,\n" +
                "  COMPADCAC.`COMPAD33` AS `是否分体`,\n" +
                "  COMPADCAC.`COMPADC1` AS `仪表规格`,\n" +
                "  COMPADCAC.`COMPADC2` AS `仪表传感元件材质`,\n" +
                "  COMPADCAC.`COMPADC4` AS `仪表安装方式`,\n" +
                "  COMPADCAC.`COMPADC5` AS `仪表插入深度`,\n" +
                "  COMPADCAC.`COMPADC10` AS `仪表数据表编号`,\n" +
                "  COMPADCAC.`COMPADC11` AS `是否自带电缆`,\n" +
                "  COMPADCAC.`COMPADC12` AS `自带电缆芯数截面积`,\n" +
                "  COMPADCAC.`COMPADC15` AS `自带电缆长度`,\n" +
                "  COMPADCAC.`COMPADC16` AS `自带电缆连接方式`,\n" +
                "  COMPADCAC.`COMPADCAC1` AS `热电阻分度号`,\n" +
                "  COMPADCAC.`COMPADCAC2` AS `仪表延长段长度`,\n" +
                "  COMPADCAC.`COMPADCAC3` AS `传感器类型`\n" +
                " FROM  (select id from `1516`.COMPADCAC where id=${COMP1} and COMP8=${COMP1}) AS COMPADCAC1\n" +
                "  LEFT JOIN `1516`.ROOM AS ROOM ON ROOM.ROOM2 = COMPADCAC1.COMP8\n" +
                "  LEFT JOIN `1516`.FLOO AS FLOO ON FLOO.FLOO1 = ROOM.ROOM50\n" +
                "  LEFT JOIN `1516`.BULD AS BULD ON BULD.BULD2 = FLOO.FLOO3\n";

        String sql2="SELECT a1.id\n" +
                " from  (select id,name,ff,alter_create_time from `1516`.dataplatform_attr_instance where uuid='sdfsdfwfsdffsdf' group by id) as a1\n" +
                " where id=3 and name='sdfsf' and ff='sdf'  group by a1.id having a1.id>=2 limit  10,100";

        String sql3="SELECT  *\n" +
                " from  1516.dataplatform_attr_instance\n" +
                " limit  10";
        ScistorSQLParser parser = new ScistorSQLParser(sql3, null);
        ScistorResult result = parser.getResult();

        if(result instanceof ScistorSelectResult){

            ScistorSelectResult re = (ScistorSelectResult) result;


            parser.addCondition(findOwner(re)+"."+"alter_create_time","${alter_create_time}", SQLBinaryOperator.GreaterThan);
            System.out.println(parser.getChangedSql());
           /* List<ScistorColumn> columns = re.getConditionColumns();
            if(columns!=null)
                for(ScistorColumn column : columns){
                    System.out.println("condition column:"+column);
                }
            if(columns!=null){
                for(ScistorColumn co : columns){
                    if(co instanceof ScistorTextColumn){
                        ScistorTextColumn cc = (ScistorTextColumn)co;
                        while(cc.hasNext()){
                            String value = cc.getNextValue();
                            String columnName = co.getName();
                            String tablename = co.getOwner();
                        }
                    }else if(co instanceof ScistorColumn){

                    }
                }
            }
            List<ScistorSelectColumn> selectedColumns = re.getSelectColumns();
            List<ScistorColumn> whereColumns = re.getConditionColumns();*/
            /*for(ScistorSelectColumn column : selectedColumns){
                System.out.println("selectTable:"+column.getOwner());
                System.out.println("selectedColumn:"+column.getName());
            }*/
          /*  for(ScistorColumn column : whereColumns){
                System.out.println("whereTable:"+column.getOwner());
                System.out.println("whereColumn:"+column.getName());
            }*/
        }
        System.out.println("================");
        long end = System.currentTimeMillis();
    }
    public static String findOwner(ScistorSelectResult selectResult){
        Map<String, Long> countMap =   selectResult.getSelectColumns().stream().collect(Collectors.
                groupingBy(ScistorSelectColumn::getOwner,Collectors.counting() ));
        AtomicReference<String> owner = new AtomicReference<>("");
        countMap.entrySet().stream().max(Map.Entry.comparingByValue()).ifPresent(item->owner.set(item.getKey()));
        return owner.get();
    }
}
