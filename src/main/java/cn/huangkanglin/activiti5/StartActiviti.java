package cn.huangkanglin.activiti5;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.Test;

import java.util.List;

/**
 * 请假流程
 */
public class StartActiviti {

    ProcessEngineConfiguration engineConfiguration =
            ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml");
    //从类加载路径中查找资源  activiti.cfg.xm文件名可以自定义
    ProcessEngine processEngine = engineConfiguration.buildProcessEngine();

    // 1.创建流程引擎
    @Test
    public void createActivitiEngine() {
        if (processEngine != null) {
            System.out.println("使用配置文件Activiti.cfg.xml获取流程引擎");
        }
    }


    // 2.部署工作流，发布工作流
    @Test
    public void deploy() {
        //获取仓库服务 ：管理流程定义
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deploy = repositoryService.createDeployment()//创建一个部署的构建器
                .addClasspathResource("askForLeave.bpmn")//从类路径中添加资源,一次只能添加一个资源
                .name("请求单流程")//设置部署的名称
                .category("办公类别")//设置部署的类别
                .deploy(); //部署工作流

        System.out.println("部署的id:" + deploy.getId());
        System.out.println("部署的名称:" + deploy.getName());
    }

    // 3.执行工作流
    @Test
    public void startProcess() {
        //指定执行我们刚才部署的工作流程
        String processDefiKey = "askForLeave";
        //取运行时服务
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //取得流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(processDefiKey);//通过流程定义的key 来执行流程
        System.out.println("流程实例id:" + pi.getId());//流程实例id
        System.out.println("流程定义id:" + pi.getProcessDefinitionId());//输出流程定义的id
    }

    // 4.1.查询任务
    @Test
    public void queryTask() {
        //任务的办理人 张三，李四
        String assignee = "李四";
        //取得任务服务
        TaskService taskService = processEngine.getTaskService();
        //创建一个任务查询对象
        TaskQuery taskQuery = taskService.createTaskQuery();
        //办理人的任务列表
        List<Task> list = taskQuery.taskAssignee(assignee)//指定办理人
                .list();
        //遍历任务列表
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务的办理人：" + task.getAssignee());
                System.out.println("任务的id   ：" + task.getId());
                System.out.println("任务的名称：" + task.getName());
            }
        } else {
            System.out.println("没有对应的任务");
        }
    }

    // 4.2.查看任务,用key的方式
    @Test
    public void queryTaskKey() {
        //取得任务服务
        TaskService taskService = processEngine.getTaskService();
        //根据接受人获取该用户的任务 ，表act_ru_task的字段assignee_为待办人
        List<Task> tasks = taskService.createTaskQuery()
                .processDefinitionKey("leaveApply").list();
        //遍历任务列表
        if (tasks != null && tasks.size() > 0) {
            for (Task task : tasks) {
                System.out.println("任务的办理人：" + task.getAssignee());
                System.out.println("任务的id   ：" + task.getId());
                System.out.println("任务的名称：" + task.getName());
            }
        } else {
            System.out.println("没有对应的任务");
        }
    }

    // 5.处理任务
    @Test
    public void compileTask() {
        String taskId = "";
        //任务的办理人
        String assignee = "张三";
        //取得任务服务
        TaskService taskService = processEngine.getTaskService();
        //创建一个任务查询对象
        TaskQuery taskQuery = taskService.createTaskQuery();
        //办理人的任务列表
        List<Task> list = taskQuery.taskAssignee(assignee)//指定办理人
                .list();
        if (list != null && list.size() > 0) {
            // 根据条件获取对应的任务id
            taskId = list.get(0).getId();
        }
        //taskId：任务id
        processEngine.getTaskService().complete(taskId);
        System.out.println("当前任务执行完毕");
    }

    // 6.查询历史任务
    @Test
    public void queryHistoryTask() throws Exception {
        //历史任务办理人
        String taskAssignee = "张三";
        // 使用办理人查询流程实例
        List<HistoricTaskInstance> list = processEngine.getHistoryService()//
                .createHistoricTaskInstanceQuery()//创建历史任务查询
                .taskAssignee(taskAssignee)//指定办理人查询历史任务
                .list();
        if (list != null && list.size() > 0) {
            for (HistoricTaskInstance task : list) {
                System.out.println("任务ID：" + task.getId());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("任务的办理人：" + task.getAssignee());
                System.out.println("执行对象ID：" + task.getExecutionId());
                System.out.println(task.getStartTime() + "　" + task.getEndTime() + "　" + task.getDurationInMillis());
            }
        }
    }


}

