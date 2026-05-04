package com.example.dhcpms.util;

import com.example.dhcpms.entity.FeeRecord;
import com.example.dhcpms.entity.FeeType;
import com.example.dhcpms.entity.Owner;
import com.example.dhcpms.entity.User;
import com.example.dhcpms.service.FeeTypeService;
import com.example.dhcpms.service.OwnerService;
import com.example.dhcpms.service.UserService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Excel 导出工具类
 */
public class ExcelExportUtil {

    /**
     * 导出费用记录到 Excel
     * @param response HTTP 响应对象
     * @param feeRecords 费用记录列表
     * @param fileName 文件名（不含扩展名）
     * @param ownerService Owner 服务
     * @param userService User 服务
     * @param feeTypeService FeeType 服务
     */
    public static void exportFeeRecordsToExcel(
            HttpServletResponse response,
            List<FeeRecord> feeRecords,
            String fileName,
            OwnerService ownerService,
            UserService userService,
            FeeTypeService feeTypeService) throws IOException {
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        // 文件名编码处理
        String encodedFileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
        response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName + ".xlsx");

        // 创建工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("费用记录");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // 创建单元格样式
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cellStyle.setWrapText(true);

            // 创建表头行
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "楼栋号", "房间号", "业主姓名", "联系电话", "费用类型",
                "金额 (元)", "截止日期", "缴费状态", "核销人员", "催缴信息", "备注"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (FeeRecord fee : feeRecords) {
                Row row = sheet.createRow(rowNum++);

                // 关联 Owner 获取楼栋号和房间号
                Owner owner = ownerService.getById(fee.getOwnerId());
                String buildingNo = "";
                String roomNo = "";
                if (owner != null) {
                    buildingNo = owner.getBuildingNo() != null ? owner.getBuildingNo() : "";
                    roomNo = owner.getRoomNo() != null ? owner.getRoomNo() : "";
                }

                // 关联 User 获取业主姓名和电话
                String ownerName = "";
                String phone = "";
                if (owner != null && owner.getUserId() != null) {
                    User ownerUser = userService.getById(owner.getUserId());
                    if (ownerUser != null) {
                        ownerName = ownerUser.getName() != null ? ownerUser.getName() : "";
                        phone = ownerUser.getPhone() != null ? ownerUser.getPhone() : "";
                    }
                }

                // 关联 FeeType 获取费用类型名称
                String feeTypeName = "";
                FeeType feeType = feeTypeService.getById(fee.getFeeTypeId());
                if (feeType != null) {
                    feeTypeName = feeType.getTypeName() != null ? feeType.getTypeName() : "";
                }

                // 关联 operator 获取核销人员姓名
                String operatorName = "";
                if (fee.getOperatorId() != null) {
                    User operator = userService.getById(fee.getOperatorId());
                    if (operator != null) {
                        operatorName = operator.getName() != null ? operator.getName() : "";
                    }
                }

                // 设置单元格值
                row.createCell(0).setCellValue(buildingNo);
                row.createCell(1).setCellValue(roomNo);
                row.createCell(2).setCellValue(ownerName);
                row.createCell(3).setCellValue(phone);
                row.createCell(4).setCellValue(feeTypeName);
                row.createCell(5).setCellValue(fee.getAmount() != null ? fee.getAmount().doubleValue() : 0.0);
                row.createCell(6).setCellValue(fee.getDueDate() != null ? fee.getDueDate().toString() : "");

                // 状态转换
                String statusDesc = fee.getStatus() == 1 ? "已缴费" :
                                   (fee.getStatus() == 2 ? "已撤销" : "待缴费");
                row.createCell(7).setCellValue(statusDesc);

                row.createCell(8).setCellValue(operatorName);

                // 催缴信息（简化处理，显示无）
                row.createCell(9).setCellValue("无");

                row.createCell(10).setCellValue(fee.getRemark() != null ? fee.getRemark() : "");

                // 应用样式
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = row.getCell(i);
                    if (cell != null) {
                        cell.setCellStyle(cellStyle);
                    }
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 限制最大宽度，避免过宽
                if (sheet.getColumnWidth(i) > 256 * 50) {
                    sheet.setColumnWidth(i, 256 * 50);
                }
            }

            // 输出到响应流
            try (OutputStream os = response.getOutputStream()) {
                workbook.write(os);
                os.flush();
            }
        }
    }
}
