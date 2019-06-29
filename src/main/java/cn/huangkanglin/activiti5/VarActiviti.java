package cn.huangkanglin.activiti5;

import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.List;

/**
 * 流程变量学习
 */
public class VarActiviti {
    ProcessEngineConfiguration engineConfiguration =
            ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml");
    //从类加载路径中查找资源  activiti.cfg.xm文件名可以自定义
    ProcessEngine processEngine = engineConfiguration.buildProcessEngine();

    // 1.部署工作流，发布工作流
    @Test
    public void deploy() {
        //获取仓库服务 ：管理流程定义
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deploy = repositoryService.createDeployment()//创建一个部署的构建器
                .addClasspathResource("apealFLow.bpmn")//从类路径中添加资源,一次只能添加一个资源
                .addClasspathResource("apealFLow.png")
                .name("流程变量的学习")//设置部署的名称
                .category("流程学习")//设置部署的类别
                .deploy(); //部署工作流

        System.out.println("部署的id:" + deploy.getId());
        System.out.println("部署的名称:" + deploy.getName());
    }

    // 2.执行工作流
    @Test
    public void startProcess() {
        //指定执行我们刚才部署的工作流程
        String processDefiKey = "AppealFill";
        //取运行时服务
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //取得流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(processDefiKey);//通过流程定义的key 来执行流程
        System.out.println("流程实例id:" + pi.getId());//流程实例id
        System.out.println("流程定义id:" + pi.getProcessDefinitionId());//输出流程定义的id
    }

    // 3.设置流程变量
    @Test
    public void setVar() {
        TaskService taskService = processEngine.getTaskService();

        // 获取第一个流程
        List<Task> tasks = taskService.createTaskQuery().list();
        Task task = tasks.get(0);

        // 使用流程变量名称和变量值设置流程变量，一次只能设置一个
        taskService.setVariable(task.getId(), "诉求类型", "请假2");
        taskService.setVariable(task.getId(), "诉求人员", "张三");
        taskService.setVariable(task.getId(), "诉求原因", "我要回家");
    }

    // 4.获取流程变量
    @Test
    public void getVar(){
        TaskService taskService = processEngine.getTaskService();

        // 获取第一个流程
        List<Task> tasks = taskService.createTaskQuery().list();
        Task task = tasks.get(0);

        // 获取变量
        Object variable = taskService.getVariable(task.getId(), "诉求类型");
        System.out.println(variable);
    }
}
