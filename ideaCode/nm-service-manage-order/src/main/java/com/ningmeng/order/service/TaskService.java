package com.ningmeng.order.service;

import com.ningmeng.framework.domain.task.NmTask;
import com.ningmeng.framework.domain.task.NmTaskHis;
import com.ningmeng.order.dao.NmTaskHisRepository;
import com.ningmeng.order.dao.NmTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by 86181 on 2020/3/17.
 */
@Service
public class TaskService {
    @Autowired
    NmTaskRepository nmTaskRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    NmTaskHisRepository nmTaskHisRepository;

    //取出前n条任务,取出指定时间之前处理的任务
    public List<NmTask> findTaskList(Date updateTime, int n){
        //设置分页参数，取出前n 条记录
        Pageable pageable = new PageRequest(0, n);
        Page<NmTask> nmTasks = nmTaskRepository.findByUpdateTimeBefore(pageable,updateTime);
        return nmTasks.getContent();
    }

    /**
     * //发送消息
     * @param nmTask1 任务对象
     * @param ex 交换机id
     * @param routingKey
     */
    @Transactional
    public void publish(NmTask nmTask1,String ex,String routingKey){
        //查询任务
        Optional<NmTask> taskOptional = nmTaskRepository.findById(nmTask1.getId());
        if(taskOptional.isPresent()){
            NmTask nmTask = taskOptional.get();
            //String exchange, String routingKey, Object object
            rabbitTemplate.convertAndSend(ex,routingKey,nmTask);
            //更新任务时间为当前时间
            nmTask.setUpdateTime(new Date());
            nmTaskRepository.save(nmTask);
        }
    }

    @Transactional
    public int getTask(String taskId,int version){
        int i = nmTaskRepository.updateTaskVersion(taskId, version);
        return i;
    }

    //删除任务
    @Transactional
    public void finishTask(String taskId){
        Optional<NmTask> taskOptional = nmTaskRepository.findById(taskId);
        if(taskOptional.isPresent()){
            NmTask nmTask = taskOptional.get();
            nmTask.setDeleteTime(new Date());
            NmTaskHis nmTaskHis = new NmTaskHis();
            BeanUtils.copyProperties(nmTask, nmTaskHis);
            nmTaskHisRepository.save(nmTaskHis);
            nmTaskRepository.delete(nmTask);
        }
    }
}
