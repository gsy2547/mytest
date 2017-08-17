package cn.itcast.bos.web.action;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.apache.struts2.convention.annotation.Result;
import org.junit.Test.None;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

import cn.itcast.bos.domain.base.Standard;
import cn.itcast.bos.service.base.StandardService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
@Controller
@Scope("prototype")
@ParentPackage("struts-default")
@Namespace("/")
public class StandardAction extends ActionSupport implements ModelDriven<Standard> {
	 
	private static final String SUCCESS = null;
	private static final String NONE = null;
	private  Standard standard=new Standard();
	public Standard getModel() {
		return standard;
	}
	@Autowired
	private StandardService standardService;
	
	// 收派标准的添加
	@Action(value="standard_save", results={@Result(name="save", type="redirect", location="/pages/base/standard.html")})
	public String save(){
		standardService.save(standard);
		return "save";
	}
	//收派标准的分页查询standardAction_pageQuery
	//提供属性驱动 接受 dategrid 的参数  page 和rows 
	private int page;
	private int rows;
	public void setPage(int page) {
		this.page = page;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
	@Action(value="standardAction_pageQuery")
	public String  pageQuery() throws IOException{
		Pageable pageable =new PageRequest(page-1, rows);
		Page<Standard> pagedate =standardService.pageQuery(pageable);
		Map<String, Object> map=new HashMap<>();
		map.put("total",pagedate.getTotalElements() );
		map.put("rows", pagedate.getContent());
		//转为json格式
		String json = JSONObject.fromObject(map).toString();
		//解决乱码问题
		ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
		ServletActionContext.getResponse().getWriter().write(json);
		return NONE;
	}
	//查询所有standard_findAll
	@Action(value="standard_findAll")
	public String findAll () throws IOException{
		List<Standard> list =standardService.findAll();
		JSONArray json = JSONArray.fromObject(list);
		ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
		ServletActionContext.getResponse().getWriter().write(json.toString());
		return NONE;
	}

}
