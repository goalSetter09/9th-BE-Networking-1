package com.cotato.networking1.service;

import com.cotato.networking1.entity.Property;
import com.cotato.networking1.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyTestDataService {

    private final PropertyRepository propertyRepository;

    public void saveProperties(MultipartFile file) throws IOException {
        List<Property> properties = parseExcelFile(file);
        propertyRepository.saveAll(properties);
    }

    private List<Property> parseExcelFile(MultipartFile file) throws IOException {
        List<Property> properties = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // 헤더 스킵

            Property property = new Property();
            property.setZipCode(readCellAsString(row.getCell(0)));

            StringBuilder roadNameAddress = new StringBuilder();
            roadNameAddress.append(readCellAsString(row.getCell(1))).append(" ")
                    .append(readCellAsString(row.getCell(2))).append(" ")
                    .append(readCellAsString(row.getCell(3))).append(" ")
                    .append(numericToStringSafe(row.getCell(4)));

            // 건물번호 부번 없는 경우 처리
            String cell5Value = numericToStringSafe(row.getCell(5));
            if (!cell5Value.isEmpty()) {
                roadNameAddress.append("-").append(cell5Value);
            }

            property.setRoadNameAddress(roadNameAddress.toString());

            String landLotNameAddress = readCellAsString(row.getCell(1)) + " " +
                    readCellAsString(row.getCell(2)) + " " +
                    readCellAsString(row.getCell(6)) + " " +
                    numericToStringSafe(row.getCell(7)) + "-" +
                    numericToStringSafe(row.getCell(8));
            property.setLandLotNameAddress(landLotNameAddress);

            properties.add(property);
        }
        return properties;
    }

    private String readCellAsString(Cell cell) {
        try {
            return cell == null || cell.getCellType() == CellType.BLANK ? "" : cell.toString().trim();
        } catch (Exception e) {
            return ""; // 값이 없거나 읽을 수 없는 경우 빈 문자열 반환
        }
    }

    private String numericToStringSafe(Cell cell) {
        try {
            return numericToString(cell);
        } catch (IllegalArgumentException e) {
            return ""; // 지정된 예외 발생시 빈 문자열 반환
        }
    }


    private String numericToString(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            throw new IllegalArgumentException("Cell is null or blank");
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            double numericValue = cell.getNumericCellValue();
            long longValue = (long) numericValue;
            return String.valueOf(longValue);
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                double numericValue = Double.parseDouble(cell.getStringCellValue());
                long longValue = (long) numericValue;
                return String.valueOf(longValue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("String cell contains non-numeric value");
            }
        } else {
            throw new IllegalArgumentException("Unsupported cell type: " + cell.getCellType());
        }
    }

}
