package org.manager.controller;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.*;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.manager.appContext.AppContext;
import org.manager.dto.OrderDTO;
import org.manager.service.OrderService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.List;
import java.util.stream.Collectors;


public class OrderTableController {
    @FXML private TextField searchOrderField;
    @FXML private TableView<OrderDTO> orderTable;
    @FXML private TableColumn<OrderDTO,String> colOrderNumber;
    @FXML private TableColumn<OrderDTO,String> colCustomer;
    @FXML private TableColumn<OrderDTO,String> colDate;
    @FXML private TableColumn<OrderDTO,Double>colTotal;
    private final ObservableList<OrderDTO>orders= FXCollections.observableArrayList();
    private final OrderService orderService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    String token= SessionManager.getToken();

    public  OrderTableController(){
        this.orderService=new OrderService();
    }

    @FXML
    public void initialize(){
         loadOrders();
         setUpOrderDataTable();
         searchOrderField.textProperty().addListener((obs,old,newValue)->filterOrders(newValue) );
    }

    @FXML
    private void goToOrder(){
        try {
            AppContext.getMainController().loadOrder();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void  setUpOrderDataTable(){
        colOrderNumber.setCellValueFactory(data-> new
                SimpleStringProperty(data.getValue().getOrderNumber()));
        colCustomer.setCellValueFactory(data->new
                SimpleStringProperty(data.getValue().getCustomerName()));
        colDate.setCellValueFactory(data->new
                SimpleStringProperty(data.getValue().getOrderDate()!=null? DATE_FORMATTER.format(data.getValue().getOrderDate()):""));
        colTotal.setCellValueFactory(data->new
                SimpleDoubleProperty(data.getValue().getTotalAmount()!=null?data.getValue().getTotalAmount().doubleValue():0.0).asObject());
        orderTable.setItems(orders);
    }
    private void loadOrders (){
        orderService.fetchOrdersAsync(token)
                .thenAccept(list-> Platform.runLater(()->{
                    orders.setAll(list);
                }))
                .exceptionally(ex-> {
                    Platform.runLater(() -> {
                        AlertUtil.showError("Erro", "Falha ao carregar pedidos: " + ex.getMessage());
                    });
                    return null;
                });

    }
    @FXML
    private void searchOrders(){
        filterOrders(searchOrderField.getText());
    }
    private void filterOrders( String query){
      if (query==null|| query.isBlank()) {
          orderTable.setItems(orders);
          return;
      }
      String lower=query.toLowerCase();
        List<OrderDTO>fiteredOrder=orders.stream()
                .filter(order-> order.getOrderNumber()!=null && order.getOrderNumber().toLowerCase().contains(lower)
                        ||order.getCustomerName()!=null  && order.getCustomerName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
        orderTable.setItems(FXCollections.observableArrayList(fiteredOrder));
    }
    @FXML
    private void exportOrderToPdf(){exportOrderPdfAndExcelFile("pdf");}
    private void exportOrderPdfAndExcelFile(String type){
        OrderDTO selectedOrder=orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder==null){
            AlertUtil.showInfo("Atenção", "Selecione uma encomenda para exportar.");
            return;
        }
        var typeFile=type.equalsIgnoreCase("pdf")?
        orderService.exportOrderPdf(selectedOrder.getId(),token):
        orderService.exportOrderExcel(selectedOrder.getId(),token);
        typeFile.thenAccept(bytes ->saveFiles(bytes,"order_"+selectedOrder.getOrderNumber() + "." + type) )
                .exceptionally(ex -> {
                    Platform.runLater(() ->{
                        System.out.println( "Falha ao exportar  encomenda" + type.toUpperCase() + ": " + ex.getMessage());
                        AlertUtil.showError("Erro", "Falha ao exportar encomenda" + type.toUpperCase() + ": " + ex.getMessage());
                    });
                    return null;

                });
    }
    @FXML
    private void exportOrderToExcel(){exportOrderPdfAndExcelFile("excel");}

    @FXML
    private void printOrder() {
        try {
            OrderDTO orderselected = orderTable.getSelectionModel().getSelectedItem();
            if (orderselected == null) {
                AlertUtil.showInfo("Atenção", "Selecione uma encomenda para imprimir.");
                return;
            }

            // Mostra alerta de progresso básico
            Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
            progressAlert.setTitle("Impressão");
            progressAlert.setHeaderText("Preparando impressão...");
            progressAlert.show();

            orderService.exportOrderPdf(orderselected.getId(), token)
                    .thenAccept(pdfBytes -> Platform.runLater(() -> {
                        progressAlert.setHeaderText("Imprimindo...");

                        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                            int totalPages = document.getNumberOfPages();

                            PrinterJob printerJob = PrinterJob.createPrinterJob();
                            if (printerJob == null || !printerJob.showPrintDialog(null)) {
                                progressAlert.close();
                                return;
                            }

                            PageLayout pageLayout = printerJob.getJobSettings().getPageLayout();
                            double pageWidth = pageLayout.getPrintableWidth();
                            double pageHeight = pageLayout.getPrintableHeight();

                            PDFRenderer renderer = new PDFRenderer(document);
                            boolean success = true;

                            for (int page = 0; page < totalPages; page++) {
                                BufferedImage image = renderer.renderImageWithDPI(page, 300);
                                Image fxImage = SwingFXUtils.toFXImage(image, null);
                                ImageView imageView = new ImageView(fxImage);
                                imageView.setPreserveRatio(true);

                                double scaleX = pageWidth / fxImage.getWidth();
                                double scaleY = pageHeight / fxImage.getHeight();
                                double scale = Math.min(scaleX, scaleY);

                                imageView.setFitWidth(fxImage.getWidth() * scale);
                                imageView.setFitHeight(fxImage.getHeight() * scale);

                                if (!printerJob.printPage(imageView)) {
                                    success = false;
                                    break;
                                }
                            }

                            progressAlert.close();

                            if (success) {
                                printerJob.endJob();
                                AlertUtil.showInfo("Sucesso",
                                        "Encomenda " + orderselected.getOrderNumber() + " impressa com sucesso! (" + totalPages + " páginas)");
                            } else {
                                AlertUtil.showError("Erro", "Falha durante a impressão.");
                            }

                        } catch (Exception e) {
                            progressAlert.close();
                            AlertUtil.showError("Erro", "Erro ao imprimir: " + e.getMessage());
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            progressAlert.close();
                            AlertUtil.showError("Erro", "Falha ao imprimir encomenda: " + ex.getMessage());
                        });
                        return null;
                    });
        } catch (Exception e) {
            AlertUtil.showError("Erro", "Erro inesperado: " + e.getMessage());
        }
    }

    private void saveFiles(byte[] bytes,String fileName){
        new Thread(()->{
            try{
                File file =new File(System.getProperty("user.home")+"/Downloads/"+fileName);
                try(FileOutputStream fos=new FileOutputStream(file)){
                    fos.write(bytes);
                }
                Platform.runLater(()->{
                    Platform.runLater(() ->
                            AlertUtil.showInfo("Sucesso", "Arquivo salvo em: " + file.getAbsolutePath()));
                });
            } catch ( IOException e) {
                Platform.runLater(() ->
                        AlertUtil.showError("Erro", "Falha ao salvar arquivo: " + e.getMessage()));
            }
        }).start();
    }
}
