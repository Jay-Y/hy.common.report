package org.hy.common.report.bean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.report.ExcelHelp;





/**
 * 报表模板信息 
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-03-15
 * @version     v1.0
 */
public class RTemplate implements Comparable<RTemplate>
{
    
    /** 模板名称 */
    private String                     name;
    
    /** Excel文件版本(1.xls  2.xlsx) */
    private String                     excelVersion;
    
    /** 模板文件的名称(全路径+文件名称) */
    private String                     excelFileName;
    
    /** 报表模板对应的工作表索引位置（下标从零开始） */
    private Integer                    sheetIndex;
    
    /** 报表标题的开始行号（包括此行）。下标从零开始 */
    private Integer                    titleBeginRow;
    
    /** 报表标题的结束行号（包括此行）。下标从零开始 */
    private Integer                    titleEndRow;
    
    /** 报表数据的开始行号（包括此行）。下标从零开始 */
    private Integer                    dataBeginRow;
    
    /** 报表数据的结束行号（包括此行）。下标从零开始 */
    private Integer                    dataEndRow;
    
    /** 合计内容的开始行号（包括此行）。下标从零开始 */
    private Integer                    totalBeginRow;
    
    /** 合计内容的结束行号（包括此行）。下标从零开始 */
    private Integer                    totalEndRow;
    
    /** 报表数据的Java类型 */
    private String                     dataClass;
    
    /** 值的标记。默认为一个冒号：":" */
    private String                     valueSign;
    
    
    /** 报表模板信息对应的工作表对象(一般只初始加载一次) */
    private HSSFSheet                  templateSheet; 
    
    /** 解释的值的反射方法集合 */
    private Map<String ,MethodReflect> valueMethods;
    
    
    
    public RTemplate()
    {
        this.sheetIndex    = 0;
        this.templateSheet = null;
        this.excelVersion  = "xls";
        this.valueSign     = ":";
        this.valueMethods  = new LinkedHashMap<String ,MethodReflect>();
    }
    
    
    
    /**
     * 获取报表模板对应的工作表
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-03-16
     * @version     v1.0
     *
     * @return
     */
    public synchronized HSSFSheet getTemplateSheet()
    {
        if ( null == this.templateSheet )
        {
            List<HSSFSheet> v_Sheets = ExcelHelp.read(this.excelFileName);
            
            if ( Help.isNull(v_Sheets) )
            {
                this.templateSheet = null;
            }
            else
            {
                this.templateSheet = v_Sheets.get(this.sheetIndex);
            }
            
            this.init();
        }
        
        return this.templateSheet;
    }
    
    
    
    /**
     * 初始化
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-03-17
     * @version     v1.0
     *
     */
    private void init()
    {
        try
        {
            Map<String ,Object> v_ExcelDatas = ExcelHelp.readDatas(this.getTemplateSheet());
            List<String>        v_TempDatas  = Help.toListKeys(v_ExcelDatas);
            Class<?>            v_JavaClass  = Help.forName(this.dataClass);
            
            for (int i=v_TempDatas.size()-1; i>=0; i--)
            {
                String v_Value = v_TempDatas.get(i);
                
                if ( this.valueSign.equals(v_Value.substring(0 ,this.valueSign.length())) && v_Value.length() >= this.valueSign.length() + 1 )
                {
                    String v_ValueName = v_Value.substring(this.valueSign.length());
                    
                    MethodReflect v_MethodReflect = new MethodReflect(v_JavaClass ,v_ValueName ,true ,MethodReflect.$NormType_Getter);
                    this.valueMethods.put(v_Value ,v_MethodReflect);
                }
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
    }
    
    
    
    public boolean isExists(String i_ValueName)
    {
        return this.valueMethods.containsKey(i_ValueName);
    }
    
    
    
    public Object getValue(String i_ValueName ,Object i_Datas)
    {
        MethodReflect v_MethodReflect = this.valueMethods.get(i_ValueName);
        Object        v_Ret           = "";
        
        if ( v_MethodReflect != null )
        {
            try
            {
                v_Ret = v_MethodReflect.invokeForInstance(i_Datas);
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 获取标题的总行数
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-03-17
     * @version     v1.0
     *
     * @return
     */
    public int getRowCountTitle()
    {
        return this.getRowCount(this.titleBeginRow ,this.titleEndRow);
    }
    
    
    
    /**
     * 获取数据的总行数
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-03-17
     * @version     v1.0
     *
     * @return
     */
    public int getRowCountData()
    {
        return this.getRowCount(this.dataBeginRow ,this.dataEndRow);
    }
    
    
    
    /**
     * 获取合计的总行数
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-03-17
     * @version     v1.0
     *
     * @return
     */
    public int getRowCountTotal()
    {
        return this.getRowCount(this.totalBeginRow ,this.totalEndRow);
    }
    
    
    
    /**
     * 获取的总行数
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-03-17
     * @version     v1.0
     *
     * @param i_BeginRow  开始行号。下标从零开始
     * @param i_EndRow    结束行号。下标从零开始
     * @return
     */
    public int getRowCount(Integer i_BeginRow ,Integer i_EndRow)
    {
        if ( null == i_BeginRow 
          || null == i_EndRow )
        {
            return 0;
        }
        
        if ( i_BeginRow.intValue() == i_EndRow.intValue() )
        {
            return 1;
        }
        
        return i_EndRow.intValue() - i_BeginRow.intValue() + 1;
    }
    
    
    
    /**
     * 获取：模板名称
     */
    public String getName()
    {
        return name;
    }

    
    /**
     * 设置：模板名称
     * 
     * @param name 
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    
    /**
     * 获取：Excel文件版本(1.xls  2.xlsx)
     */
    public String getExcelVersion()
    {
        return excelVersion;
    }

    
    /**
     * 设置：Excel文件版本(1.xls  2.xlsx)
     * 
     * @param excelVersion 
     */
    public void setExcelVersion(String excelVersion)
    {
        this.excelVersion = excelVersion;
    }


    /**
     * 获取：模板文件的名称(全路径+文件名称)
     */
    public String getExcelFileName()
    {
        return excelFileName;
    }

    
    /**
     * 设置：模板文件的名称(全路径+文件名称)
     * 
     * @param excelFileName 
     */
    public void setExcelFileName(String excelFileName)
    {
        this.excelFileName = excelFileName;
    }

    
    /**
     * 获取：报表模板对应的工作表索引位置（下标从零开始）
     */
    public Integer getSheetIndex()
    {
        return sheetIndex;
    }

    
    /**
     * 设置：报表模板对应的工作表索引位置（下标从零开始）
     * 
     * @param sheetIndex 
     */
    public void setSheetIndex(Integer sheetIndex)
    {
        this.sheetIndex = sheetIndex;
    }

    
    /**
     * 获取：报表标题的开始行号（包括此行）。下标从零开始
     */
    public Integer getTitleBeginRow()
    {
        return titleBeginRow;
    }

    
    /**
     * 设置：报表标题的开始行号（包括此行）。下标从零开始
     * 
     * @param i_TitleBeginRow 
     */
    public void setTitleBeginRow(Integer i_TitleBeginRow)
    {
        this.titleBeginRow = i_TitleBeginRow;
        this.titleEndRow   = i_TitleBeginRow;
    }

    
    /**
     * 获取：报表标题的结束行号（包括此行）。下标从零开始
     */
    public Integer getTitleEndRow()
    {
        return titleEndRow;
    }

    
    /**
     * 设置：报表标题的结束行号（包括此行）。下标从零开始
     * 
     * @param titleEndRow 
     */
    public void setTitleEndRow(Integer titleEndRow)
    {
        this.titleEndRow = titleEndRow;
    }


    /**
     * 获取：报表数据的开始行号（包括此行）。下标从零开始
     */
    public Integer getDataBeginRow()
    {
        return dataBeginRow;
    }

    
    /**
     * 设置：报表数据的开始行号（包括此行）。下标从零开始
     * 
     * @param i_DataBeginRow 
     */
    public void setDataBeginRow(Integer i_DataBeginRow)
    {
        this.dataBeginRow = i_DataBeginRow;
        this.dataEndRow   = i_DataBeginRow;
    }

    
    /**
     * 获取：报表数据的结束行号（包括此行）。下标从零开始
     */
    public Integer getDataEndRow()
    {
        return dataEndRow;
    }

    
    /**
     * 设置：报表数据的结束行号（包括此行）。下标从零开始
     * 
     * @param dataEndRow 
     */
    public void setDataEndRow(Integer dataEndRow)
    {
        this.dataEndRow = dataEndRow;
    }

    
    /**
     * 获取：合计内容的开始行号（包括此行）。下标从零开始
     */
    public Integer getTotalBeginRow()
    {
        return totalBeginRow;
    }

    
    /**
     * 设置：合计内容的开始行号（包括此行）。下标从零开始
     * 
     * @param i_TotalBeginRow 
     */
    public void setTotalBeginRow(Integer i_TotalBeginRow)
    {
        this.totalBeginRow = i_TotalBeginRow;
        this.totalEndRow   = i_TotalBeginRow;
    }

    
    /**
     * 获取：合计内容的结束行号（包括此行）。下标从零开始
     */
    public Integer getTotalEndRow()
    {
        return totalEndRow;
    }

    
    /**
     * 设置：合计内容的结束行号（包括此行）。下标从零开始
     * 
     * @param totalEndRow 
     */
    public void setTotalEndRow(Integer totalEndRow)
    {
        this.totalEndRow = totalEndRow;
    }

    
    /**
     * 获取：报表数据的Java类型
     */
    public String getDataClass()
    {
        return dataClass;
    }

    
    /**
     * 设置：报表数据的Java类型
     * 
     * @param dataClass 
     */
    public void setDataClass(String dataClass)
    {
        this.dataClass = dataClass;
    }


    /**
     * 获取：值的标记。默认为一个冒号：":"
     */
    public String getValueSign()
    {
        return valueSign;
    }

    
    /**
     * 设置：值的标记。默认为一个冒号：":"
     * 
     * @param valueSign 
     */
    public void setValueSign(String valueSign)
    {
        this.valueSign = valueSign;
    }



    @Override
    public int compareTo(RTemplate i_Other)
    {
        if ( i_Other == null )
        {
            return 1;
        }
        else if ( this == i_Other )
        {
            return 0;
        }
        else
        {
            if ( this.excelFileName == null )
            {
                return -1;
            }
            
            return this.excelFileName.compareTo(i_Other.getExcelFileName());
        }
    }



    @Override
    public int hashCode()
    {
        if ( this.excelFileName != null )
        {
            return this.excelFileName.hashCode();
        }
        
        return super.hashCode();
    }



    @Override
    public boolean equals(Object i_Other)
    {
        if ( this == i_Other )
        {
            return true;
        }
        else if ( i_Other == null )
        {
            return false;
        }
        else if ( i_Other instanceof RTemplate )
        {
            if ( this.excelFileName == null )
            {
                return false;
            }
            
            return this.excelFileName.equals(((RTemplate)i_Other).getExcelFileName());
        }
        else
        {
            return false;
        }
    }



    @Override
    public String toString()
    {
        return Help.NVL(this.excelFileName);
    }
    
}
