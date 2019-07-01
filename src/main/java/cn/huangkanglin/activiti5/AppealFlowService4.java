package cn.huangkanglin.activiti5;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 诉求流程管理服务类
 * 完善原因：修改流程变量支持taskId
 */
public class AppealFlowService4 {
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

        System.out.println("部署ID：" + deploy.getId()); // 部署Id---47501
        System.out.println("部署名称：" + deploy.getName()); // 部署名称---0001:诉求
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

        System.out.println("流程实例ID:" + pi.getId());//流程实例ID   50001
        System.out.println("流程定义ID:" + pi.getProcessDefinitionId());//流程定义ID  AppealFill:5:47504
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

    // 4.1 处理任务- 跳转到next任务 --修
    @Test
    public void completeTaskNext() {
        // 任务ID
        String taskId = "55002";
        TaskService taskService = processEngine.getTaskService();
        // 1.认领当前任务
        taskService.claim(taskId,"0030");

        // 2.完成任务的同时，设置流程变量，让流程变量判断连线该如何执行
        Map<String, Object> variables = new HashMap<String, Object>();
        // 其中message对应sequenceFlow.bpmn中的${step=='next'}，next 对应流程变量的值
        variables.put("step", "next");



        // 3.完成任务，流程引擎通过流程变量里面的step=='next'自动跳转到下一步
        taskService.complete(taskId, variables);

        System.out.println("完成next任务：任务ID：" + taskId);
    }

    // 4.2 处理任务- 跳转到previous任务
    @Test
    public void completeTaskPrevious() {
        //任务ID
        String taskId = "10002";

        // 1.完成任务的同时，设置流程变量，让流程变量判断连线该如何执行
        Map<String, Object> variables = new HashMap<String, Object>();
        // 其中message对应sequenceFlow.bpmn中的${step=='previous'}，previous 对应流程变量的值
        variables.put("step", "previous");

        // 2.完成任务，流程引擎通过流程变量里面的step=='previous'自动跳转到上一步
        processEngine.getTaskService()
                .complete(taskId, variables);
        System.out.println("完成next任务：任务ID：" + taskId);
    }

    // 4.2 处理任务- 跳转到previous任务
    @Test
    public void completeTaskEnd() {
        //任务ID
        String taskId = "32503";

        // 1.完成任务的同时，设置流程变量，让流程变量判断连线该如何执行
        Map<String, Object> variables = new HashMap<String, Object>();
        // 其中message对应sequenceFlow.bpmn中的${step=='previous'}，previous 对应流程变量的值
        variables.put("step", "end");
        variables.put("taskId", taskId);


        // 2.完成任务，流程引擎通过流程变量里面的step=='previous'自动跳转到上一步
        processEngine.getTaskService()
                .complete(taskId, variables);
        System.out.println("完成next任务：任务ID：" + taskId);
    }

    //5.根据用户Id查找所有的代办任务
    @Test
    public void findMyTaskListByUserId() {
        List<Task> candidateUserList = processEngine.getTaskService()
                .createTaskQuery()//根据用户id查询任务
                .taskCandidateUser("0030")
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


    // 6.通过任务id获取节点下一步的连线
    @Test
    public void taskOutCome() {
        String taskId = "10002";
        List<String> outComeListByTaskId = findOutComeListByTaskId(taskId);
        System.out.println(outComeListByTaskId.toString());
    }


    /**
     *
     * 已知任务ID，查询ProcessDefinitionEntiy对象，从而获取当前任务完成之后的连线名称，并放置到List<String>集合中
     * 应用场景：这个用来，加载页面上不同的按钮
     */
    public List<String> findOutComeListByTaskId(String taskId) {
        //返回存放连线的名称集合
        List<String> list = new ArrayList<String>();
        //1:使用任务ID，查询任务对象
        Task task = processEngine.getTaskService().createTaskQuery()//
                .taskId(taskId)//使用任务ID查询
                .singleResult();

        //2：获取流程定义ID
        String processDefinitionId = task.getProcessDefinitionId();
        //3：查询ProcessDefinitionEntiy对象
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) processEngine.getRepositoryService().getProcessDefinition(processDefinitionId);
        //使用任务对象Task获取流程实例ID
        String processInstanceId = task.getProcessInstanceId();
        //使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
        ProcessInstance pi = processEngine.getRuntimeService().createProcessInstanceQuery()//
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();
        //获取当前活动的id
        String activityId = pi.getActivityId();
        //4：获取当前的活动
        ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);
        //5：获取当前活动完成之后连线的名称
        List<PvmTransition> pvmList = activityImpl.getOutgoingTransitions();
        if (pvmList != null && pvmList.size() > 0) {
            for (PvmTransition pvm : pvmList) {
                String name = (String) pvm.getProperty("name");
                if (StringUtils.isNotBlank(name)) {
                    list.add(name);
                } else {
                    list.add("默认提交");
                }
            }
        }
        return list;
    }

    /**
     * 历史任务查询
     */
    @Test
    public void historyTaskList(){
        List<HistoricTaskInstance> list=processEngine.getHistoryService() // 历史任务Service
                .createHistoricTaskInstanceQuery() // 创建历史任务实例查询
                //.taskAssignee("0001") // 指定办理人
                .taskCandidateUser("0020") // 指定用户
                .finished() // 查询已经完成的任务
                .list();
        for(HistoricTaskInstance hti:list){
            System.out.println("任务ID:"+hti.getId());
            System.out.println("流程实例ID:"+hti.getProcessInstanceId());
            System.out.println("班里人："+hti.getAssignee());
            System.out.println("创建时间："+hti.getClaimTime());
            System.out.println("结束时间："+hti.getEndTime());
            System.out.println("===========================");
        }
    }
}
