package cn.itcast.bos.web.action;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

import cn.itcast.bos.domain.base.SubArea;
import cn.itcast.bos.service.base.SubAreaService;
import cn.itcast.bos.utils.FileUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
@Controller
@Scope("prototype")
@ParentPackage("struts-default")
@Namespace("/")
public class SubAreaAction extends ActionSupport implements ModelDriven<SubArea> {
	private SubArea model=new SubArea();
	public SubArea getModel() {
		return model;
	}
	@Autowired
	private SubAreaService subAreaService;
	
	//分区的保存subareaAction_save
	@Action(value="subareaAction_save", results={@Result(name="save", type="redirect", location="/pages/base/sub_area.html")})
	public String save(){
		subAreaService.save(model);
		return "save";
	}
	
	//提供属性驱动 接受 dategrid 的参数  page 和rows 
	private int page;
	private int rows;
	public void setPage(int page) {
		this.page = page;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
	//查询加分页subArea_pageQuery
   @Action(value="subArea_pageQuery")
   public String pageQuery() throws IOException{
	   Pageable pageable =new PageRequest(page-1, rows);
	   Page<SubArea>pagedate=subAreaService.pageQuery(pageable);
	   Map<String, Object>map=new HashMap<>();
	   map.put("total", pagedate.getTotalElements());
	   map.put("rows", pagedate.getContent());
	   //转为json格式
	   JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setExcludes(new String[]{"subareas","fixedArea"});
		JSONObject json = JSONObject.fromObject(map, jsonConfig);
		//解决乱码问题
		ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
		ServletActionContext.getResponse().getWriter().write(json.toString());;
	   return NONE;
   }
   //分区数据导出subArea_expData
   @Action(value="subArea_expData")
   public String expData() throws IOException{
	   //要想导出先查询数据库所有的数据
	   List<SubArea> list=subAreaService.findAll();
	   ////创建出一个xls文件
	   HSSFWorkbook hssf=new HSSFWorkbook();
	   //创建一个标签页
	   HSSFSheet sheet = hssf.createSheet("分区数据");
	   //创建数据的标题行
	   Row titleRow = sheet.createRow(0);
	   //给每个标题行赋值
	   titleRow.createCell(0).setCellValue("分区编号");
	   titleRow.createCell(1).setCellValue("所属省份");
	   titleRow.createCell(2).setCellValue("所属城市");
	   titleRow.createCell(3).setCellValue("所属区域");
	   //遍历分区集合，展示到xls中的每一列上。
	   for (SubArea subArea : list) {
		   //创建数据行
		   HSSFRow dateRow = sheet.createRow(sheet.getLastRowNum()+1);
		   //给每列赋值
		   dateRow.createCell(0).setCellValue(subArea.getId());
		   dateRow.createCell(1).setCellValue(subArea.getArea().getProvince());
		   dateRow.createCell(2).setCellValue(subArea.getArea().getCity());
		   dateRow.createCell(3).setCellValue(subArea.getArea().getDistrict());
	   }
	   //创建输入流  【导出文件遵循一个流两个头，一个流指的是输出流，两个头一个指定文件名，一个指定文件类型】
	   ServletOutputStream out=ServletActionContext.getResponse().getOutputStream();
	   //设置文件的mime类型，告诉浏览器当前的文件的类型
	   ServletActionContext.getResponse().setContentType("application/vnd.ms-excel");
	   //中文文件名
 	   String filename = "分区数据.xls";
 	   //获取当前浏览器的类型和版本User-Agent
 	   String agent = ServletActionContext.getRequest().getHeader("User-Agent");
 	   //用工具类对在不同浏览器下识别中文进行处理
 	   filename = FileUtils.encodeDownloadFilename(filename, agent);
 	   //设置中文文件名
		ServletActionContext.getResponse().setHeader("Content-Disposition", "attachment; filename="+filename);
		//使用poi封装的方法把xls文件通过输出流写出去
		hssf.write(out);
	   return NONE;
   }
   @Action("subArea_pie")
	public String pie() throws IOException{
		List<Object[]> list = subAreaService.findPieData();
		//转为json格式的数据
		JsonConfig jsonConfig=new JsonConfig();
		jsonConfig.setExcludes(new String[]{});
		JSONArray json = JSONArray.fromObject(list,jsonConfig);
		//解决乱码问题
		ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
		ServletActionContext.getResponse().getWriter().print(json);
		return NONE;
	}
}
