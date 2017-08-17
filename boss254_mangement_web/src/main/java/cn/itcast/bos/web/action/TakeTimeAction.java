package cn.itcast.bos.web.action;

import java.io.IOException;
import java.util.List;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

import cn.itcast.bos.domain.base.TakeTime;
import cn.itcast.bos.service.base.TakeTimeService;
import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
@Controller
@Scope("prototype")
@ParentPackage("struts-default")
@Namespace("/")
public class TakeTimeAction extends ActionSupport implements ModelDriven<TakeTime> {

	private TakeTime model=new TakeTime();
	public TakeTime getModel() {
		return model;
	}
	@Autowired
	private TakeTimeService takeTimeService;
	
	//关联快递员 选择收派时间takeTime_findAll
	@Action(value="takeTime_findAll")
	public String findAll() throws IOException{
		List<TakeTime> list = takeTimeService.findAll();
		//转为json数据格式
		JsonConfig jsonConfig=new JsonConfig();
		jsonConfig.setExcludes(new String[]{});
		JSONArray json = JSONArray.fromObject(list, jsonConfig);
		//解决乱码问题
		ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
		//响应
		ServletActionContext.getResponse().getWriter().print(json);
		return NONE;
	}
}
