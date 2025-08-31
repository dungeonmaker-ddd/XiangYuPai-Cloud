package com.xypai.sms.api;

import com.xypai.sms.dto.SmsSendRequestDTO;
import com.xypai.sms.dto.SmsSendResponseDTO;
import com.xypai.sms.dto.SmsTemplateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 📱 短信服务API接口
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@FeignClient(
        name = "xypai-sms-service",
        path = "/api/v1/sms"
)
@Tag(name = "短信服务API", description = "提供短信发送、模板管理等功能")
public interface SmsServiceApi {

    /**
     * 📤 发送短信
     */
    @Operation(
            summary = "发送短信",
            description = "支持单个或批量发送短信，可选择同步或异步方式",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "短信发送请求",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SmsSendRequestDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "验证码短信",
                                            summary = "发送验证码短信示例",
                                            value = """
                                                    {
                                                      "templateCode": "REGISTER_VERIFY",
                                                      "phoneNumbers": ["13800138000"],
                                                      "templateParams": {
                                                        "code": "123456",
                                                        "minutes": "5"
                                                      },
                                                      "signCode": "DEFAULT_SIGN",
                                                      "async": false
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "营销短信",
                                            summary = "发送营销短信示例",
                                            value = """
                                                    {
                                                      "templateCode": "MARKETING_PROMOTION",
                                                      "phoneNumbers": ["13800138000", "13900139000"],
                                                      "templateParams": {
                                                        "productName": "新春大礼包",
                                                        "discount": "8折"
                                                      },
                                                      "signCode": "MARKETING_SIGN",
                                                      "loadBalanceStrategy": "WEIGHT_RANDOM",
                                                      "async": true,
                                                      "businessTag": "SPRING_PROMOTION"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "发送成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SmsSendResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "429", description = "发送频率超限"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/send")
    SmsSendResponseDTO sendSms(@Valid @RequestBody SmsSendRequestDTO request);

    /**
     * 📋 获取模板列表
     */
    @Operation(
            summary = "获取短信模板列表",
            description = "分页获取短信模板列表，支持按类型和状态筛选"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SmsTemplateDTO.class)
                    )
            )
    })
    @GetMapping("/templates")
    List<SmsTemplateDTO> getTemplates(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer page,

            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") Integer size,

            @Parameter(description = "模板类型", example = "VERIFICATION")
            @RequestParam(required = false) String templateType,

            @Parameter(description = "状态", example = "ACTIVE")
            @RequestParam(required = false) String status
    );

    /**
     * 📄 获取模板详情
     */
    @Operation(
            summary = "获取模板详情",
            description = "根据模板编号获取模板详细信息"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SmsTemplateDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "模板不存在")
    })
    @GetMapping("/templates/{templateCode}")
    SmsTemplateDTO getTemplate(
            @Parameter(description = "模板编号", example = "REGISTER_VERIFY", required = true)
            @PathVariable String templateCode
    );

    /**
     * ➕ 创建模板
     */
    @Operation(
            summary = "创建短信模板",
            description = "创建新的短信模板，需要等待审核通过后才能使用"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "创建成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SmsTemplateDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "409", description = "模板编号已存在")
    })
    @PostMapping("/templates")
    SmsTemplateDTO createTemplate(@Valid @RequestBody SmsTemplateDTO template);

    /**
     * 🔄 更新模板
     */
    @Operation(
            summary = "更新短信模板",
            description = "更新现有的短信模板信息"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "更新成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SmsTemplateDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "模板不存在"),
            @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    @PutMapping("/templates/{templateCode}")
    SmsTemplateDTO updateTemplate(
            @Parameter(description = "模板编号", example = "REGISTER_VERIFY", required = true)
            @PathVariable String templateCode,
            @Valid @RequestBody SmsTemplateDTO template
    );

    /**
     * 🗑️ 删除模板
     */
    @Operation(
            summary = "删除短信模板",
            description = "删除指定的短信模板"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "模板不存在"),
            @ApiResponse(responseCode = "409", description = "模板正在使用中，无法删除")
    })
    @DeleteMapping("/templates/{templateCode}")
    void deleteTemplate(
            @Parameter(description = "模板编号", example = "REGISTER_VERIFY", required = true)
            @PathVariable String templateCode
    );

    /**
     * 📊 查询发送状态
     */
    @Operation(
            summary = "查询发送状态",
            description = "根据任务ID查询短信发送状态"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "查询成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SmsSendResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "任务不存在")
    })
    @GetMapping("/send/{taskId}/status")
    SmsSendResponseDTO getSendStatus(
            @Parameter(description = "任务ID", example = "task_987654321", required = true)
            @PathVariable String taskId
    );
}
