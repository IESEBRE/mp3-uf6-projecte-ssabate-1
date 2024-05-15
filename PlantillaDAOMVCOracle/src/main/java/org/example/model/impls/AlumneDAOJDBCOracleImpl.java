package org.example.model.impls;

import org.example.model.daos.DAO;
import org.example.model.entities.Alumne;
import org.example.model.exceptions.DAOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class AlumneDAOJDBCOracleImpl implements DAO<Alumne> {


    @Override
    public Alumne get(Long id) throws DAOException {

        //Declaració de variables del mètode
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        Alumne estudiant = null;

        //Accés a la BD usant l'API JDBC
        try {

            con = DriverManager.getConnection(
                    "jdbc:oracle:thin:@//localhost:1521/xe",
                    "C##HR",
                    "HR"
            );
//            st = con.prepareStatement("SELECT * FROM estudiant WHERE id=?;");
            st = con.createStatement();
//            st = con.prepareStatement("SELECT * FROM estudiant WHERE id=?;");
//            st.setLong(1, id);
            rs = st.executeQuery("SELECT * FROM ALUMNES;");
//            estudiant = new Alumne(rs.getLong(1), rs.getString(2));
//            st.close();
            if (rs.next()) {
                estudiant = new Alumne(Long.valueOf(rs.getString(1)), rs.getString(2));
            }
        } catch (SQLException throwables) {
            throw new DAOException(1);
        } finally {
            try {
                if (rs != null) rs.close();
                if (st != null) st.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                throw new DAOException(1);
            }

        }
        return estudiant;
    }

    @Override
    public List<Alumne> getAll() throws DAOException {
        //Declaració de variables del mètode
        List<Alumne> estudiants = new ArrayList<>();

        //Accés a la BD usant l'API JDBC
        try (Connection con = DriverManager.getConnection(
                "jdbc:oracle:thin:@//localhost:1521/xe",
                "C##HR",
                "HR"
        );
             PreparedStatement st = con.prepareStatement("SELECT * FROM ALUMNES");
             ResultSet rs = st.executeQuery();
        ) {

            while (rs.next()) {
                estudiants.add(new Alumne(rs.getLong("id"), rs.getString("nom"),rs.getDouble("pes"),
                        new TreeSet<Alumne.Matricula>()));
            }
        } catch (SQLException throwables) {
            int tipoError = throwables.getErrorCode();
            //System.out.println(tipoError+" "+throwables.getMessage());
            switch(throwables.getErrorCode()){
                case 17002: //l'he obtingut posant un sout en el throwables.getErrorCode()
                    tipoError = 0;
                    break;
                default:
                    tipoError = 1;  //error desconegut
            }
            throw new DAOException(tipoError);
        }


        return estudiants;
    }

    @Override
    public void save(Alumne obj) throws DAOException {
        String insertSQL = "INSERT INTO ALUMNES (id,nom,pes,matriculat) VALUES (?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(
                "jdbc:oracle:thin:@//localhost:1521/xe",
                "C##HR",
                "HR"
        );
             PreparedStatement st = con.prepareStatement(insertSQL)
        ) {
            st.setLong(1, obj.getId());
            st.setString(2, obj.getNom());
            st.setDouble(3, obj.getPes());
            st.setString(4, "t");
            st.executeUpdate();
        } catch (SQLException throwables) {
            throw new DAOException(1);
        }
    }
}
