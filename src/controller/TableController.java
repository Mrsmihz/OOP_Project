package controller;
import myutilities.MapComparator;
import views.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import model.*;
public class TableController implements KeyListener, ItemListener, TableModelListener {
    private TableGUI tableGUI;
    private MainController mc;
    private ArrayList<HashMap<String, Object>> data;
    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JComboBox searchComboBox, sortComboBox;
    public TableController(MainController mc){
        this.mc = mc;
        this.tableGUI = new TableGUI(this);
        setComponents();
    }
    private void setComponents(){
        this.searchField = tableGUI.getSearchField();
        this.tableModel = tableGUI.getTableModel();
        this.searchComboBox = tableGUI.getSearchComboBox();
        this.sortComboBox = tableGUI.getSortComboBox();
    }
    public void keyTyped(KeyEvent keyEvent){
    }
    public void itemStateChanged(ItemEvent itemEvent){
        if(itemEvent.getStateChange() == ItemEvent.SELECTED && itemEvent.getSource().equals(tableGUI.getSortComboBox())){
            String item = (String)itemEvent.getItem();
            tableGUI.getItemTable().getRowSorter().setSortKeys(null);
            updateTableBySort(sortedData(item));
        }
        else if (itemEvent.getStateChange() == ItemEvent.SELECTED && itemEvent.getSource().equals(tableGUI.getSearchComboBox())){
            String item = (String)itemEvent.getItem();
            if (item.equals("Search By")){
                updateTableBySearch(searchedData());
                tableGUI.getSearchField().setText("");
            }
        }
    }
    public void keyPressed(KeyEvent keyEvent){
        if (keyEvent.getSource().equals(searchField) && (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)){
            updateTableBySearch(searchedData());
        }
        else if (keyEvent.getKeyCode() == KeyEvent.VK_DELETE){
            int row = tableGUI.getItemTable().getSelectedRow();
            int[] rows = tableGUI.getItemTable().getSelectedRows();
            if (rows.length > 1){
                deleteItems(rows);
            }
            else{
                deleteItem(row);
            }
        }
    }
    public void keyReleased(KeyEvent keyEvent){
    }
    private void deleteItems(int[] rows){
        int count = rows.length;
        String msg = String.format("Do you want to delete %d items ?", count);
        int selected = JOptionPane.showConfirmDialog(mc.getMainFrame(), msg);
        if (selected == JOptionPane.YES_OPTION){
            for (int row : rows){
                int id = Integer.parseInt(String.valueOf(tableModel.getValueAt(row, 0)));
                String name = (String)tableModel.getValueAt(row, 1);
                String type = (String)tableModel.getValueAt(row, 2);
                double price = Double.parseDouble(String.valueOf(tableModel.getValueAt(row, 3)));
                double weight = Double.parseDouble(String.valueOf(tableModel.getValueAt(row, 4)));
                int quantity = Integer.parseInt(String.valueOf(tableModel.getValueAt(row, 5)));
                Item temp = new Item(mc.getUser().getId(), name, type, price, weight, quantity);
                try{
                    if (!mc.getUser().deleteItem(temp, id)){
                        JOptionPane.showMessageDialog(mc.getMainFrame(), "Deleted failed.");
                        return;
                    }
                }
                catch (SQLException e){
                    JOptionPane.showMessageDialog(mc.getMainFrame(), "Deleted failed.");
                    return;
                }
            }
            updateTable();
            JOptionPane.showMessageDialog(mc.getMainFrame(), count+"Items deleted.");
        }
    }
    private void deleteItem(int row){
        int id = Integer.parseInt(String.valueOf(tableModel.getValueAt(row, 0)));
        String name = (String)tableModel.getValueAt(row, 1);
        String type = (String)tableModel.getValueAt(row, 2);
        double price = Double.parseDouble(String.valueOf(tableModel.getValueAt(row, 3)));
        double weight = Double.parseDouble(String.valueOf(tableModel.getValueAt(row, 4)));
        int quantity = Integer.parseInt(String.valueOf(tableModel.getValueAt(row, 5)));
        Item temp = new Item(mc.getUser().getId(), name, type, price, weight, quantity);
        int selected = JOptionPane.showConfirmDialog(mc.getMainFrame(), "Do you want to delete item ?");
        if (selected == JOptionPane.YES_OPTION){
            try{
                if (mc.getUser().deleteItem(temp, id)){
                    JOptionPane.showMessageDialog(mc.getMainFrame(), "Item deleted.");
                    updateTable();
                }
                else{
                    JOptionPane.showMessageDialog(mc.getMainFrame(), "Deleted failed.");
                }
            }
            catch (SQLException e){
                JOptionPane.showMessageDialog(mc.getMainFrame(), "Deleted failed.");
            }
        }
    }
    private HashMap<String, Object> getItem(int id){
        try{
            ResultSet rs = mc.getUser().getItem(id);
            ResultSetMetaData meta = rs.getMetaData();
            int col = meta.getColumnCount();
            HashMap<String, Object> row = new HashMap<>(col);
            for (int i=1;i<=col;i++){
                row.put(meta.getColumnName(i), rs.getObject(i));
            }
            return row;
        }
        catch (SQLException e){
            System.out.println(e);
            return null;
        }
    }
    private ArrayList<HashMap<String, Object>> getDataFromDB(){
        try{
            ResultSet rs = mc.getUser().getAllItem();
            ResultSetMetaData meta = rs.getMetaData();
            int col = meta.getColumnCount();
            ArrayList<HashMap<String, Object>> list = new ArrayList<>();
            while (rs.next()){
                HashMap<String, Object> row = new HashMap<>(col);
                for (int i = 1;i<=col;i++){
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                list.add(row);
            }
            return list;
        }
        catch (SQLException error){
            System.out.println("Fetch Data Failed!");
            return null;
        }
    }
    private void addDataToModel(ArrayList<HashMap<String, Object>> data){
        for (int i = 0;i<data.size();i++){
            HashMap<String, Object> map = data.get(i);
            int item_id = (int)map.get("item_id");
            String item_name = (String)map.get("item_name");
            String item_type = (String)map.get("item_type");
            double item_price = (double)map.get("item_price");
            double item_weight = (double)map.get("item_weight");
            int quantity = (int)map.get("quantity");
            Timestamp timeStampObj = (Timestamp)map.get("inserted");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String date = dateFormat.format(timeStampObj);
            Object[] row ={item_id, item_name, item_type, item_price, item_weight, quantity, date};
            tableModel.addRow(row);
        }
    }
    private void addDataToModelBySort(ArrayList<LinkedHashMap<String, Object>> data){
        for (int i = 0;i<data.size();i++){
            LinkedHashMap<String, Object> map = data.get(i);
            int item_id = (int)map.get("ID");
            String item_name = (String)map.get("Name");
            String item_type = (String)map.get("Type");
            double item_price = (double)map.get("Price");
            double item_weight = (double)map.get("Weight");
            int quantity = (int)map.get("Quantity");
            String date = (String)map.get("Added Time");
            Object[] row ={item_id, item_name, item_type, item_price, item_weight, quantity, date};
            tableModel.addRow(row);
        }
    }
    public void updateTable(){
        tableModel.setRowCount(0);
        data = getDataFromDB();
        addDataToModel(data);
    }
    private void updateTableBySearch(ArrayList<HashMap<String, Object>> data){
        tableModel.setRowCount(0);
        addDataToModel(data);
    }
    private void updateTableBySort(ArrayList<LinkedHashMap<String, Object>> data){
        tableModel.setRowCount(0);
        addDataToModelBySort(data);
    }
    private ArrayList<LinkedHashMap<String, Object>> sortedData(String order){
        ArrayList<LinkedHashMap<String, Object>> data = new ArrayList<>();
        for (int i = 0;i<tableModel.getRowCount();i++){
            LinkedHashMap<String, Object> map = new LinkedHashMap<>(tableModel.getColumnCount());
            for (int j = 0;j<tableModel.getColumnCount();j++){
                map.put(tableModel.getColumnName(j), tableModel.getValueAt(i, j));
            }
            data.add(map);
        }
        if (order.equals("Sort By")){
            return data;
        }
        else if (order.equals("ID: Low-High")){
            Collections.sort(data, new MapComparator("ID"));
            return data;
        }
        else if (order.equals("ID: High-Low")){
            Collections.sort(data, new MapComparator("ID"));
            Collections.reverse(data);
            return data;
        }
        else if (order.equals("Name: Alphabetically")){
            Collections.sort(data, new MapComparator("Name"));
            return data;
        }
        else if (order.equals("Type: Alphabetically")){
            Collections.sort(data, new MapComparator("Type"));
            return data;
        }
        else if (order.equals("Price: Low-High")){
            Collections.sort(data, new MapComparator("Price"));
            return data;
        }
        else if (order.equals("Price: High-Low")){
            Collections.sort(data, new MapComparator("Price"));
            Collections.reverse(data);
            return data;
        }
        else if (order.equals("Weight: Low-High")){
            Collections.sort(data, new MapComparator("Weight"));
            return data;
        }
        else if (order.equals("Weight: High-Low")){
            Collections.sort(data, new MapComparator("Weight"));
            Collections.reverse(data);
            return data;
        }
        else if (order.equals("Quantity: Low-High")){
            Collections.sort(data, new MapComparator("Quantity"));
            return data;
        }
        else if (order.equals("Quantity: High-Low")){
            Collections.sort(data, new MapComparator("Quantity"));
            Collections.reverse(data);
            return data;
        }
        else{
            return null;
        }
    }
    private ArrayList<HashMap<String, Object>> searchedData(){
        ArrayList<HashMap<String, Object>> data, result = new ArrayList<>();
        String s = (String)searchComboBox.getItemAt(searchComboBox.getSelectedIndex());
        if (searchField.getText().equals("Type words then press enter")){
            return getDataFromDB();
        }
        if (s.equals("Search By")){
            return getDataFromDB();
        }
        else if (s.equals("ID")){
            try{
                Integer.parseInt(searchField.getText());
            }
            catch (NumberFormatException e){
                JOptionPane.showMessageDialog(mc.getMainFrame(), "Invalid ID Value !", "Alert", JOptionPane.WARNING_MESSAGE);
                return getDataFromDB();
            }
            data = getDataFromDB();
            for (HashMap<String, Object> map : data) {
                if ((int) map.get("item_id") == Integer.parseInt(searchField.getText())) {
                    result.add(map);
                }
            }
            return result;
        }
        else if (s.equals("Name")){
            data = getDataFromDB();
            for (HashMap<String, Object> map : data) {
                if (((String)map.get("item_name")).toLowerCase().equals(searchField.getText().toLowerCase())) {
                    result.add(map);
                }
            }
            return result;
        }
        else if (s.equals("Type")){
            data = getDataFromDB();
            for (HashMap<String, Object> map : data) {
                if (((String)map.get("item_type")).toLowerCase().equals(searchField.getText().toLowerCase())) {
                    result.add(map);
                }
            }
            return result;
        }
        else{
            return null;
        }
    }

    public TableGUI getTableGUI() {
        return tableGUI;
    }

    public void setTableGUI(TableGUI tableGUI) {
        this.tableGUI = tableGUI;
    }

    public MainController getMc() {
        return mc;
    }

    public void setMc(MainController mc) {
        this.mc = mc;
    }

    public ArrayList<HashMap<String, Object>> getData() {
        return data;
    }

    public void setData(ArrayList<HashMap<String, Object>> data) {
        this.data = data;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public void setSearchField(JTextField searchField) {
        this.searchField = searchField;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public void setTableModel(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
    }

    public JComboBox getSearchComboBox() {
        return searchComboBox;
    }

    public void setSearchComboBox(JComboBox searchComboBox) {
        this.searchComboBox = searchComboBox;
    }

    public JComboBox getSortComboBox() {
        return sortComboBox;
    }

    public void setSortComboBox(JComboBox sortComboBox) {
        this.sortComboBox = sortComboBox;
    }

    private void updateOnChange(int row){
        try {
            int id = Integer.parseInt(String.valueOf(tableModel.getValueAt(row, 0)));
            String name = (String)tableModel.getValueAt(row, 1);
            String type = (String)tableModel.getValueAt(row, 2);
            double price = Double.parseDouble(String.valueOf(tableModel.getValueAt(row, 3)));
            double weight = Double.parseDouble(String.valueOf(tableModel.getValueAt(row, 4)));
            int quantity = Integer.parseInt(String.valueOf(tableModel.getValueAt(row, 5)));
            Item temp = new Item(mc.getUser().getId(), name, type, price, weight, quantity);
            HashMap<String, Object> item = getItem(id);
            if (item.get("item_name").equals(temp.getItem_name())
            && item.get("item_type").equals(temp.getItem_type())
            && item.get("item_price").equals(temp.getItem_price())
            && item.get("item_weight").equals(temp.getItem_weight())
            && item.get("quantity").equals(temp.getQuantity())){
                return;
            }
            else{
                if (mc.getUser().modifyItem(temp, id)){
                    JOptionPane.showMessageDialog(mc.getMainFrame(), "Update item successfully.");
                }
                else{
                    JOptionPane.showMessageDialog(mc.getMainFrame(), "Update item failed !");
                }
            }

        }
        catch (NullPointerException | NumberFormatException e){
            JOptionPane.showMessageDialog(mc.getMainFrame(), "Invalid Values !", "Warning", JOptionPane.WARNING_MESSAGE);
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(mc.getMainFrame(), "Update Item failed !", "Alert", JOptionPane.WARNING_MESSAGE);
        }
    }
    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE){
            int row = e.getFirstRow();
            updateOnChange(row);
            updateTable();
        }
    }
}
