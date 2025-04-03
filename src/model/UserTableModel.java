package model;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import model.User;

public class UserTableModel extends AbstractTableModel {

    private List<User> users;
    private String[] columns = {"ID", "Ime", "Priimek", "Email", "Telefon", "Naslov", "Admin"};

    public UserTableModel(List<User> users) {
        this.users = users;
    }

    @Override
    public int getRowCount() {
        return users.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        User user = users.get(rowIndex);
        switch (columnIndex) {
            case 0: return user.getId();
            case 1: return user.getIme();
            case 2: return user.getPriimek();
            case 3: return user.getEmail();
            case 4: return user.getTelefon();
            case 5: return user.getNaslov();
            case 6: return user.isAdmin() ? "Yes" : "No";
            default: return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }
}
