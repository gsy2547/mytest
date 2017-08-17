package cn.itcast.bos.web.action.system;

import java.io.IOException;
import java.util.List;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

import cn.itcast.bos.domain.system.Permission;
import cn.itcast.bos.service.system.PermissionService;
import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
@Controller
@Scope("prototype")
@ParentPackage("struts-default")
@Namespace("/")
public class PermissionAction extends ActionSupport implements ModelDriven<Permission> {

	private Permission model=new Permission();
	
	public Permission getModel() {
		
		return model;
	}
	@Autowired 
	private PermissionService permissionService;
	
	//系统管理权限查询列表permission_list
	@Action(value="permission_list")
	public String list() throws IOException{
		List<Permission>list=permissionService.findAll();
		//转为json格式
		JsonConfig jsonConfig=new JsonConfig();
		jsonConfig.setExcludes(new String []{"roles"});
		JSONArray json=JSONArray.fromObject(list, jsonConfig);
		//解决乱码问题
		ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
		ServletActionContext.getResponse().getWriter().print(json);
		return NONE;
	}
	
	@Action(value="permission_save", results={@Result(name="save", type="redirect", location="/pages/system/permission.html")})
	public String save(){
		permissionService.save(model);
		System.out.println("进来了");
		return "save";
	}

}
