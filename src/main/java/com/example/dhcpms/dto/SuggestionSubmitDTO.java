package com.example.dhcpms.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class SuggestionSubmitDTO {

    @NotBlank(message = "意见类型不能为空")
    private String type;  // complaint/suggestion/repair/consultation

    @NotBlank(message = "意见标题不能为空")
    @Size(min = 2, max = 100, message = "标题长度在 2-100 个字符之间")
    private String title;

    @NotBlank(message = "意见内容不能为空")
    @Size(min = 10, max = 500, message = "内容长度在 10-500 个字符之间")
    private String content;

    private String images;  // 图片路径，多张用逗号分隔

    @Size(max = 20, message = "联系电话长度不能超过 20 个字符")
    private String contactPhone;

    private Long userId;
    private Long ownerId;
}