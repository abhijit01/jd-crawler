package org.abhijit.jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtility {

	protected static Workbook workbook;
	
	public ExcelUtility() throws IOException {
		createWorkBook();
	}

	public static Workbook createWorkBook() throws IOException {

		workbook = new XSSFWorkbook();
		List<String> shopColList = new ArrayList<String>();
		shopColList.add("Name");
		shopColList.add("Contact");
		shopColList.add("Address");
		shopColList.add("Locality");
		shopColList.add("Street Address");
		shopColList.add("City");
		shopColList.add("Rating");
		//shopColList.add("Latitude");
		//shopColList.add("Longitude");

		Sheet sheet = createSheet("StationeryShop", shopColList);
		sheet.setColumnWidth(0, 35 * 256);
		sheet.setColumnWidth(1, 35 * 256);
		sheet.setColumnWidth(2, 35 * 256);
		sheet.setColumnWidth(3, 10 * 256);
		sheet.setColumnWidth(4, 10 * 256);
		sheet.setColumnWidth(5, 10 * 256);
		sheet.setColumnWidth(6, 10 * 256);
		//sheet.setColumnWidth(6, 15 * 256);
		//sheet.setColumnWidth(7, 15 * 256);
		
		return workbook;
	}

	private static Sheet createSheet(String sheetName, List<String> columns) {
		Sheet sheet = workbook.createSheet(sheetName);
		CellStyle cellStyle = workbook.createCellStyle();
		Font cellFont = workbook.createFont();
		cellFont.setFontName("verdana");
		cellFont.setFontHeightInPoints((short) 8);
		cellStyle.setFont(cellFont);

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
		headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Font headerFont = workbook.createFont();
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerStyle.setFont(headerFont);
		Row headerRow = sheet.createRow(0);

		for (int i = 0; i < columns.size(); i++) {
			sheet.setDefaultColumnStyle(i, cellStyle);
			headerRow.createCell(i).setCellValue(columns.get(i));
			headerRow.getCell(i).setCellStyle(headerStyle);
		}

		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			workbook.getSheetAt(i).createFreezePane(0, 1);
		}

		return sheet;
	}
	
	public Sheet getSheet(String sheetName) {
		return workbook.getSheet("StationeryShop");
	}
	
	public static void main(String[] args) throws IOException {
		createWorkBook();
	}
}
