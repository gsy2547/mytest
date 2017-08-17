package cn.itcast.bos.web.action;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
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
import cn.itcast.bos.domain.base.FixedArea;
import cn.itcast.bos.service.base.FixedAreaService;
import cn.itcast.crm.domain.Customer;
import cn.itcast.crm.service.CustomerService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
@Controller
@Scope("prototype")
@ParentPackage("struts-default")
@Namespace("/")
public class FixedAreaAction_save extends ActionSupport implements ModelDriven<FixedArea> {
	
	private FixedArea model=new FixedArea();
	public FixedArea getModel() {
		return model;
	}
	@Autowired
	private FixedAreaService fixedAreaService;
	@Autowired
	private CustomerService customerService;
	
	@Action(value="fixedAreaAction_findAll")
	public String findAll(){
		List<Customer> findAll = customerService.findAll();
		System.out.println(findAll);
		return NONE;
	}
	
	// 定取得添加保存 fixedAreaAction_save
	@Action(value="fixedAreaAction_save", results={@Result(name="save", type="redirect", location="/pages/base/fixed_area.html")})
	public String save(){
		fixedAreaService.save(model);
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
	//条件查询加分页fixedAreaAction_pageQuery
	@Action(value="fixedAreaAction_pageQuery")
	public String pageQuery() throws IOException{
		//框架所需要的对象
		Pageable pageable=new PageRequest(page-1, rows);
		//从页面传来的参数定区编号也就是id
		final String id = model.getId();
		//所属的单位
		final String company = model.getCompany();
		
		final List<Predicate> list = new ArrayList<>();
		
		Specification<FixedArea>spec=new Specification<FixedArea>() {
			public Predicate toPredicate(Root<FixedArea> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				if (StringUtils.isNotBlank(id)) {
					//封装查询条件
					Predicate p1 = cb.equal(root.get("id").as(String.class), id);
					list.add(p1);
				}if (StringUtils.isNotBlank(company)) {
					Predicate p2 = cb.like(root.get(company).as(String.class), "%"+company+"%");
					list.add(p2);
				}
				if (list.size()==0) {
					return null;
				}
				Predicate[] pArr = new Predicate[list.size()];
				list.toArray(pArr);
				return cb.and(pArr);
			}
		};
		Page<FixedArea> dataPage=fixedAreaService.pageQuery(pageable,spec);
		Map<String, Object> map=new HashMap<>();
		map.put("rows", dataPage.getContent());
		map.put("total", dataPage.getTotalElements());
		//转为json数据
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setExcludes(new String[]{"subareas","couriers"});
		JSONObject json = JSONObject.fromObject(map, jsonConfig);
		//解决乱码问题
		ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
		ServletActionContext.getResponse().getWriter().write(json.toString());;
		
		return NONE;
	}
	

		@Action(value="fixedArea_findCustomerWithoutFixedArea")
		public String findCustomerWithoutFixedArea() throws IOException{
			List<Customer> list = customerService.findByFixedAreaIdIsNull();
			//转为json格式
			JsonConfig jsonConfig=new JsonConfig();
			jsonConfig.setExcludes(new String[]{});
			JSONArray json =JSONArray.fromObject(list, jsonConfig);
			//解决乱码问题
			ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
			ServletActionContext.getResponse().getWriter().print(json);
			return NONE;
		}
		
		@Action(value="fixedArea_findCustomerHasFixedArea")
		public String findCustomerHasFixedArea() throws IOException{
			List<Customer> list = customerService.findByFixedAreaId(model.getId());
			
			//转为json格式
			JsonConfig jsonConfig=new JsonConfig();
			jsonConfig.setExcludes(new String[]{});
			JSONArray json =JSONArray.fromObject(list, jsonConfig);
			//解决乱码问题
			ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
			ServletActionContext.getResponse().getWriter().print(json);
			return NONE;
		}
		
		
		private List<Integer> customerIds;
		public void setCustomerIds(List<Integer> customerIds) {
			this.customerIds = customerIds;
		}

		@Action(value="fixedArea_addCustomerToFixedArea", results={@Result(name="success", type="redirect", location="/pages/base/fixed_area.html")})
		public String addCustomerToFixedArea(){
			customerService.updateCustomerFixedAreaIdToNULL(model.getId());
			customerService.updateCustomerToFixedArea(model.getId(), customerIds);
			return SUCCESS;
		}
		
		private Integer courierId;
		private Integer takeTimeId;
		public void setCourierId(Integer courierId) {
			this.courierId = courierId;
		}
		public void setTakeTimeId(Integer takeTimeId) {
			this.takeTimeId = takeTimeId;
		}
		
		//关联快递员fixedArea_addCourierToFixedArea
		@Action(value="fixedArea_addCourierToFixedArea",results={@Result(name="addCourierToFixedArea",type="redirect",location="/pages/base/fixed_area.html")})
		public String addCourierToFixedArea(){
			fixedAreaService.addCourierToFixedArea(model.getId(), courierId, takeTimeId);
			
			return "addCourierToFixedArea";
		}
		

}
