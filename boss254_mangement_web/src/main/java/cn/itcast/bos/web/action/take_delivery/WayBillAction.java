package cn.itcast.bos.web.action.take_delivery;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

import cn.itcast.bos.domain.take_delivery.WayBill;
import cn.itcast.bos.service.take_delivery.BillService;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

@Controller
@Namespace("/")
@ParentPackage(value="struts-default")
@Scope("prototype")
public class WayBillAction extends ActionSupport implements ModelDriven<WayBill> {

	private WayBill model=new WayBill();
	public WayBill getModel() {
		return model;
	}
	
	private File wayBillFile;

	public File getWayBillFile() {
		return wayBillFile;
	}
	public void setWayBillFile(File wayBillFile) {
		this.wayBillFile = wayBillFile;
	}
	@Autowired
	public BillService billService;
	
	@Action("waybill_batchImport")
	public String batchImport() throws IOException{
		
		 String flag="1";
		
		try {
			FileInputStream s= new FileInputStream(wayBillFile);
			//获得工作簿对象
			HSSFWorkbook hwk = new HSSFWorkbook(s);
			//获得工作表
			HSSFSheet hs = hwk.getSheetAt(0);
			
			List<WayBill> list = new ArrayList<>();
			
			//遍历工作表的所有行
			for (Row row : hs) {
				WayBill wb = new WayBill();
				if(row.getRowNum()!=0){
					
					wb.setGoodsType(row.getCell(1).getStringCellValue());
					wb.setSendProNum(row.getCell(2).getStringCellValue());
					wb.setSendName(row.getCell(3).getStringCellValue());
					wb.setSendMobile(row.getCell(4).getStringCellValue());
					wb.setSendAddress(row.getCell(5).getStringCellValue());
					wb.setRecName(row.getCell(6).getStringCellValue());
					wb.setRecMobile(row.getCell(7).getStringCellValue());
					wb.setRecCompany(row.getCell(8).getStringCellValue());
					wb.setRecAddress(row.getCell(9).getStringCellValue());

					list.add(wb);
				}
			}
			billService.save(list);
			
		} catch (Exception e) {
			e.printStackTrace();
			flag="0";
		}
		ServletActionContext.getResponse().getWriter().println(flag);
		return NONE;
	}
	
		
	private int page;
	private int rows;
	public void setPage(int page) {
		this.page = page;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
	//运单导入分页列表wayBill_pageQuery
	@Action(value="wayBill_pageQuery")
	public String pageQuery() throws IOException{
		//封装框架所需要的对象
		Pageable pageable=new PageRequest(page-1, rows);
		// 查询所有的
		Page<WayBill>dataPage=billService.findAll(pageable);
		Map<String, Object> map=new HashMap<>();
		map.put("rows", dataPage.getContent());
		map.put("total", dataPage.getTotalElements());
		//转为json数据
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setExcludes(new String[]{"subareas"});
		JSONObject json = JSONObject.fromObject(map, jsonConfig);
		//解决乱码问题
		ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
		ServletActionContext.getResponse().getWriter().write(json.toString());;
		return NONE;
	}
}
