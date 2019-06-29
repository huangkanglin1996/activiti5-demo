package cn.huangkanglin.activiti5;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 诉求流程管理服务类
 */
public class AppealFlowService {
    ProcessEngineConfiguration engineConfiguration =
            ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml");
    // 获取流程引擎
    ProcessEngine processEngine = engineConfiguration.buildProcessEngine();

    // 1.创建诉求
    @Test
    public void createAppealFlow() {
        // 诉求人id 到时候传入
        String appealId = "0001";

        //获取仓库服务 ：管理流程定义
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("apealFlow.bpmn")
                .addClasspathResource("apealFlow.png")
                .name(appealId + ":诉求")//设置部署的名称
                .category("诉求处理")//设置部署的类别
                .deploy(); //部署工作流

        System.out.println("部署ID：" + deploy.getId()); // 部署Id---25001
        System.out.println("部署名称：" + deploy.getName()); // 部署名称---0001：诉求
    }


    // 2.启动流程请求
    @Test
    public void startAppealFlow() {
        //流程定义的key
        String processDefinitionKey = "AppealFill";

        // 设置流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("inputUser", "0001"); // 诉求填写
        variables.put("claimProcessing", "0020,0021,0022,0023,0024,0025"); // 诉求处理
        variables.put("departmentalProcessing", "0030,0031,0032,0033,0034,0035"); // 部门处理
        variables.put("appealReview", "0040,0041,0042,0043,0044,0045"); // 诉求审核

        // 启动流程
        ProcessInstance pi = processEngine.getRuntimeService()
                .startProcessInstanceByKey(processDefinitionKey, variables);

        System.out.println("流程实例ID:" + pi.getId());//流程实例ID   45001
        System.out.println("流程定义ID:" + pi.getProcessDefinitionId());//流程定义ID  AppealFill:4:42504
    }

    // 3.查询任务
    @Test
    public void queryTask() {
        String assignee = "0001";
        List<Task> list = processEngine.getTaskService()//与正在执行的任务管理相关的Service
                .createTaskQuery()//创建任务查询对象
                /**查询条件（where部分）*/
                .taskAssignee(assignee)//指定个人任务查询，指定办理人
                /**排序*/
                .orderByTaskCreateTime().asc()//使用创建时间的升序排列
                /**返回结果集*/
                .list();//返回列表
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID:" + task.getId());
                System.out.println("任务名称:" + task.getName());
                System.out.println("任务的创建时间:" + task.getCreateTime());
                System.out.println("任务的办理人:" + task.getAssignee());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("流程定义ID:" + task.getProcessDefinitionId());
                System.out.println("########################################################");
            }
        } else {
            System.out.println("没有对应的任务");
        }
    }

    // 4.处理任务
    @Test
    public void completeTask() {
        //任务ID
        String taskId = "45008";
        String IsAgree = "批准";

        //完成任务的同时，设置流程变量，使用流程变量用来指定完成任务后，下一个连线，对应sequenceFlow.bpmn文件中${message=='不重要'}
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("outcome", IsAgree);

        processEngine.getTaskService()//与正在执行的任务管理相关的Service
                .complete(taskId, variables);
        System.out.println("完成任务：任务ID：" + taskId);


    }

    //5.根据用户Id查找所有的代办任务
    @Test
    public void findMyTaskListByUserId() {
        List<Task> candidateUserList = processEngine.getTaskService()
                .createTaskQuery()//根据用户id查询任务
                .taskCandidateUser("0020")
                .list();

        if (candidateUserList != null && candidateUserList.size() > 0) {
            for (Task task : candidateUserList) {
                System.out.println("任务ID:" + task.getId());
                System.out.println("任务名称:" + task.getName());
                System.out.println("任务的创建时间:" + task.getCreateTime());
                System.out.println("任务的办理人:" + task.getAssignee());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("流程定义ID:" + task.getProcessDefinitionId());
                System.out.println("########################################################");
            }
        } else {
            System.out.println("没有对应的任务");
        }
    }
}
