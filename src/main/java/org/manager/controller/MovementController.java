package org.manager.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import org.manager.dto.MovementResponseDTO;
import org.manager.service.MovementService;
import org.manager.session.SessionManager;
import org.manager.util.AlertUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MovementController {

    @FXML private TableView<MovementResponseDTO> movementTable;
    @FXML private TableColumn<MovementResponseDTO, Long> colId;
    @FXML private TableColumn<MovementResponseDTO, String> colDescription;
    @FXML private TableColumn<MovementResponseDTO, String> colUsername;
    @FXML private TableColumn<MovementResponseDTO, String> colType;
    @FXML private TableColumn<MovementResponseDTO, String> colOrigin;
    @FXML private TableColumn<MovementResponseDTO, String> colStatus;
    @FXML private TableColumn<MovementResponseDTO, Integer> colQuantity;
    @FXML private TableColumn<MovementResponseDTO, LocalDateTime> colDate;

    @FXML private TextField txtSearch;

    @FXML private DatePicker dateStart;
    @FXML private DatePicker dateEnd;

    private final MovementService movementService = new MovementService();
    private final ObservableList<MovementResponseDTO> movementData = FXCollections.observableArrayList();
    private final ObservableList<MovementResponseDTO> allMovementsData = FXCollections.observableArrayList();

    private String token = SessionManager.getToken();
    Long companyId = SessionManager.getCurrentCompanyId();
    private MovementResponseDTO selectedMovement;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        setupTable();
        setupSearch();
        loadMovements();
    }

    /** Configura a tabela **/
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colOrigin.setCellValueFactory(new PropertyValueFactory<>("origin"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        colDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(dateFormatter));
            }
        });

        movementTable.setItems(movementData);

        movementTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> selectedMovement = newSel
        );
    }

    /** Carrega todos os movimentos da empresa da sessão **/
    private void loadMovements() {
        if (token == null) return;

        if (companyId == null) {
            AlertUtil.showError("Erro", "Empresa não encontrada na sessão.");
            return;
        }

        movementService.fetchByCompanyAsync(companyId, token)
                .thenAccept(list -> Platform.runLater(() -> {
                    allMovementsData.setAll(list);
                    movementData.setAll(list);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", "Falha ao carregar movimentos"));
                    ex.printStackTrace();
                    return null;
                });
    }

    /** Busca texto **/
    private void setupSearch() {
        txtSearch.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                movementData.setAll(allMovementsData);
                return;
            }

            String lower = newValue.toLowerCase();

            List<MovementResponseDTO> filtered = allMovementsData.stream()
                    .filter(m ->
                            (m.getDescription() != null && m.getDescription().toLowerCase().contains(lower)) ||
                                    (m.getUsername() != null && m.getUsername().toLowerCase().contains(lower)) ||
                                    (m.getType() != null && m.getType().toLowerCase().contains(lower)) ||
                                    (m.getOrigin() != null && m.getOrigin().toLowerCase().contains(lower)) ||
                                    (m.getStatus() != null && m.getStatus().toLowerCase().contains(lower))
                    )
                    .toList();

            movementData.setAll(filtered);
        });
    }

    /** Filtra por data, respeitando a empresa da sessão **/
    @FXML
    private void filterByDate() {
        if (dateStart.getValue() == null || dateEnd.getValue() == null) {
            AlertUtil.showError("Erro", "Selecione a data inicial e final.");
            return;
        }

        LocalDate startDate = dateStart.getValue();
        LocalDate endDate = dateEnd.getValue();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        if (start.isAfter(end)) {
            AlertUtil.showError("Erro", "A data inicial não pode ser posterior à data final.");
            return;
        }

        if (companyId == null) {
            AlertUtil.showError("Erro", "Empresa não encontrada na sessão.");
            return;
        }

        movementService.fetchByCompanyAndDateAsync(companyId, start, end, token)
                .thenAccept(list -> Platform.runLater(() -> movementData.setAll(list)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        String msg = ex.getMessage() != null ? ex.getMessage() : "Erro desconhecido";
                        AlertUtil.showError("Erro ao filtrar", msg);
                    });
                    ex.printStackTrace();
                    return null;
                });
    }

    /** Limpar filtro **/
    @FXML
    private void clearFilter() {
        dateStart.setValue(null);
        dateEnd.setValue(null);
        movementData.setAll(allMovementsData);
    }

    /** Deletar movimento **/
    @FXML
    private void deleteMovement() {
        if (selectedMovement == null) {
            AlertUtil.showError("Erro", "Selecione um movimento.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Confirmação", "Deseja deletar este movimento?");
        if (!confirmed) return;

        movementService.deleteAsync(selectedMovement.getId(), token)
                .thenAccept(ok -> Platform.runLater(() -> {
                    if (ok) {
                        AlertUtil.showInfo("Sucesso", "Movimento removido!");
                        loadMovements();
                    } else {
                        AlertUtil.showError("Erro", "Falha ao remover movimento.");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertUtil.showError("Erro", ex.getMessage()));
                    return null;
                });
    }
}
