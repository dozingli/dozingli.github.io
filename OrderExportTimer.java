package com.ainfinit.retail.phantom.operation.export.timer;

import com.ainfinit.retail.api.task.base.BaseExport;
import com.ainfinit.retail.api.task.dto.ExportTaskRecordDTO;
import com.ainfinit.retail.phantom.common.utils.JsonUtil;
import com.ainfinit.retail.phantom.operation.order.biz.OrderExportBiz;
import com.ainfinit.retail.phantom.operation.order.biz.OrderFeedbackBiz;
import com.ainfinit.retail.phantom.operation.order.rest.OrderController;
import com.ainfinit.retail.phantom.operation.order.rest.OrderFeedbackController;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: duanzekang
 * @Date: 2024/7/27 16:52
 */
@Slf4j
@Component
public class OrderExportTimer extends BaseExport {

    @Autowired
    private OrderExportBiz orderExportBiz;

    @Autowired
    private OrderFeedbackBiz orderFeedbackBiz;

    /**
     * 订单导出
     */
    @XxlJob("asyncExportOrderHandler")
    public void asyncExportOrderHandler() {
        String jobParam = XxlJobHelper.getJobParam();
        log.info("order export start - {}", jobParam);
        try {
            ExportTaskRecordDTO task = this.checkAndGet(jobParam);
            OrderController.OrderQuery query = JsonUtil.toObject(task.getParams(), OrderController.OrderQuery.class);
            String fileUrl = orderExportBiz.asynExportOrder(query, task.genFileName());
            log.info("order export file url - {}", fileUrl);
            XxlJobHelper.handleSuccess(fileUrl);
        } catch (Exception e) {
            log.error("asyncExportOrderHandler error ,", e);
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    /**
     * 反馈订单导出
     */
    @XxlJob("asyncExportOrderFeedbackHandler")
    public void asyncExportOrderFeedbackHandler() {
        String jobParam = XxlJobHelper.getJobParam();
        log.info("order feedback export start - {}", jobParam);
        try {
            ExportTaskRecordDTO task = this.checkAndGet(jobParam);
            OrderFeedbackController.Query query = JsonUtil.toObject(task.getParams(), OrderFeedbackController.Query.class);
            String fileUrl = orderFeedbackBiz.asyncExportOrderFeedback(query, task.genFileName());
            log.info("order feedback export file url - {}", fileUrl);
            XxlJobHelper.handleSuccess(fileUrl);
        } catch (Exception e) {
            log.error("asyncExportOrderFeedbackHandler error ,", e);
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    /**
     * 异常订单导出
     */
    @XxlJob("asyncExportMerchantProcessHandler")
    public void asyncExportMerchantProcessHandler() {
        String jobParam = XxlJobHelper.getJobParam();
        log.info("merchant process export start - {}", jobParam);
        try {
            ExportTaskRecordDTO task = this.checkAndGet(jobParam);
            OrderController.OrderQuery query = JsonUtil.toObject(task.getParams(), OrderController.OrderQuery.class);
            String fileUrl = orderExportBiz.handleAsyncExportMerchantProcess(query, task.genFileName());
            log.info("merchant process export file url - {}", fileUrl);
            XxlJobHelper.handleSuccess(fileUrl);
        } catch (Exception e) {
            log.error("asyncExportMerchantProcessHandler error ,", e);
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    /**
     * 订单识别数据
     */
    @XxlJob("asyncExportOrderIdentifyHandler")
    public void asyncExportOrderIdentifyHandler() {
        String jobParam = XxlJobHelper.getJobParam();
        log.info("order identify export start - {}", jobParam);
        try {
            ExportTaskRecordDTO task = this.checkAndGet(jobParam);
            OrderController.ReportQuery query = JsonUtil.toObject(task.getParams(), OrderController.ReportQuery.class);
            String fileUrl = orderExportBiz.asyncExportOrderIdentifyHandler(query, task.genFileName());
            log.info("order identify export file url - {}", fileUrl);
            XxlJobHelper.handleSuccess(fileUrl);
        } catch (Exception e) {
            log.error("asyncExportOrderIdentifyHandler error ,", e);
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

}
