package cn.itcast.bos.web.action;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

import cn.itcast.bos.domain.base.Courier;
import cn.itcast.bos.domain.base.Standard;
import cn.itcast.bos.service.base.CourierService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

@Controller
@Scope("prototype")
@ParentPackage("struts-default")
@Namespace("/")
public class CourierAction extends ActionSupport implements ModelDriven<Courier> {

	private Courier model=new Courier();
	public Courier getModel() {
		return model;
	}
	 @Autowired
	 private CourierService courierService;
	//添加快递管理员 courier_save
	@Action(value="courier_save",results={@Result(name="save", type="redirect", location="/pages/base/courier.html")})
	public String save (){
		courierService.save(model);
		return "save";
	}
	private int page;
	private int rows;
	public void setPage(int page) {
		this.page = page;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
	//分页查询所有的快递管员courier_pageQuery
	@Action(value="courier_pageQuery")
	public String pageQuery() throws IOException{
		//封装框架所需要的对象
		Pageable pageable=new PageRequest(page-1, rows);
		//条件查询的工号
		final String courierNum = model.getCourierNum();
		//所属单位
	    final String company = model.getCompany();
	    //类型
	    final String type = model.getType();
	    //收派标准
	    final Standard standard = model.getStandard();
	    
	    //new一个list集合来存放所有条件，最终用来转成数组
	    final List<Predicate> list = new ArrayList<>();
  		//创建Specification一个匿名实现类
	    Specification<Courier> spec=new Specification<Courier>() {

			public Predicate toPredicate(Root<Courier> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				//System.out.println(cb);
				//StringUtils.isNotBlank(courierNum) 表示判断该变量是否为null或者""
				if (StringUtils.isNotBlank(courierNum)) {
					Predicate p1 = cb.equal(root.get("courierNum").as(String.class), courierNum);
					list.add(p1);
				}
				if (StringUtils.isNotBlank(company)) {
					Predicate p2 = cb.like(root.get("company").as(String.class),"%"+company+"%");
					list.add(p2);
				}
				if (StringUtils.isNotBlank(type)) {
					Predicate p3 = cb.equal(root.get(type).as(String.class), type);
					list.add(p3);
				}
				if(standard!=null && StringUtils.isNotBlank(standard.getName())){
					//通过root.join("standard");获取一个join对象，也就是相当于，
					//我们从courier对象去联查standard对象，最终获取到standard对象【join】。
					Join<Object, Object> join = root.join("standard");
					Predicate p4 = cb.like(join.get("name").as(String.class), "%"+standard.getName()+"%");
					list.add(p4);
				}
					Predicate[] pArr = new Predicate[list.size()];
					//list.toArray(pArr)执行完之后，pArr就已经有值了.
					list.toArray(pArr);
					//cb.and(pArr)表示数值内部的条件用and来链接
					return cb.and(pArr);
			}
		};
		Page<Courier> dataPage=courierService.pageQuery(pageable,spec);
		Map<String, Object> map=new HashMap<>();
		map.put("rows", dataPage.getContent());
		map.put("total", dataPage.getTotalElements());
		//转为json数据
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setExcludes(new String[]{"fixedAreas"});
		JSONObject json = JSONObject.fromObject(map, jsonConfig);
		//解决乱码问题
		ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
		ServletActionContext.getResponse().getWriter().write(json.toString());;
		return NONE;
	}
	//把所选择的快递人员作废courier_delBatch
	private String ids;
	public void setIds(String ids) {
		this.ids = ids;
	}
	@Action(value="courier_delBatch", results={@Result(name="delBatch", type="redirect", location="/pages/base/courier.html")})
	public String delBatch(){
		courierService.delBatch(ids);
		return "delBatch";
	}
	//把所选择的快递员还原courier_restore
	@Action(value="courier_restore", results={@Result(name="restore", type="redirect", location="/pages/base/courier.html")})
	public String restore(){
		courierService.restore(ids);
		return "restore";
	}
	
	//关联快递员 查询所有的快递员courier_findAll
	@Action(value="courier_findAll")
	public String findAll() throws IOException{
		List<Courier> list =courierService.findAll();
		//转为json格式的数据
		JsonConfig jsonConfig=new JsonConfig();
		jsonConfig.setExcludes(new String[]{"fixedAreas"});
		JSONArray json = JSONArray.fromObject(list,jsonConfig);
		//解决乱码问题
		ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
		ServletActionContext.getResponse().getWriter().print(json);
		return NONE;
	}
	
}
