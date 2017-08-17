package cn.itcast.bos.web.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

import cn.itcast.bos.domain.base.Area;
import cn.itcast.bos.service.base.AreaService;
import cn.itcast.bos.utils.PinYin4jUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;


@Controller
@Scope("prototype")
@ParentPackage("struts-default")
@Namespace("/")
public class AreaAction extends ActionSupport implements ModelDriven<Area> {

	private Area model=new Area();
	
	public Area getModel() {
		return model;
	}
	
	@Autowired
	private AreaService areaService;
	
	//接受从页面传来的文件名称
	private File xlsfile;
	//提供一个set方法
	public void setXlsfile(File xlsfile) {
		this.xlsfile = xlsfile;
	}
	@Action(value="area_impfile")
	public String impfile() throws IOException{
		String flag = "1";
		try {
			//创建输入流
			FileInputStream s=new FileInputStream(xlsfile);
			//创建一个hssf对象
			HSSFWorkbook hssf=new HSSFWorkbook(s);
			//获得取得的第一个标签页
			HSSFSheet sheet = hssf.getSheetAt(0);
			List<Area>list=new ArrayList<Area>();
			//遍历
			for (Row row : sheet) {
				if (row.getRowNum()!=0) {
					
					Area area=new Area(row.getCell(0).getStringCellValue(),
							row.getCell(1).getStringCellValue(),
							row.getCell(2).getStringCellValue(),
							row.getCell(3).getStringCellValue(),
							row.getCell(4).getStringCellValue());
					
					String province = row.getCell(1).getStringCellValue();
					String city = row.getCell(2).getStringCellValue();
					String district = row.getCell(3).getStringCellValue();
					//去掉省市区最后的一个字
					province=province.substring(0, province.length()-1);
					city=city.substring(0, city.length()-1);
					district=district.substring(0, district.length()-1);
					//获取简码的数据格式
					String[] shortcodeArr = PinYin4jUtils.getHeadByString(province+city+district);
					//把数组变为字符串
					String shortcode = StringUtils.join(shortcodeArr);
					//获得城市编码
					String citycode = PinYin4jUtils.hanziToPinyin(district, "");
					
					area.setShortcode(shortcode);
					area.setCitycode(citycode);
					list.add(area);
				}
			}
			areaService.save(list);
		} catch (Exception e) {
			e.printStackTrace();
			flag = "0";
		}
		//把标记的数据响应到页面
		ServletActionContext.getResponse().getWriter().write(flag);
		return NONE;
	
	}
	

	private String q;
	public void setQ(String q) {
		this.q = q;
	}
	//添加分区的选择区域areaAction_listajax
	@Action(value="areaAction_listajax")
	public String listajax() throws IOException{
		List<Area>list=null;
		
		if (q!=null && q!="") {
			String qq = q.toUpperCase();
			list = areaService.findByShortcodeLike("%"+qq+"%");
		} else {
			list=areaService.findAll();
		}
		
		
		//转为json数据
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setExcludes(new String[]{"subareas"});
		JSONArray json = JSONArray.fromObject(list, jsonConfig);
		//解决乱码问题
		ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
		ServletActionContext.getResponse().getWriter().print(json);
		
		return NONE;
	}
	
	
}
