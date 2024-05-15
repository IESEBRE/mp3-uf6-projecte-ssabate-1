package org.example.controller;

import org.example.model.entities.Alumne;
import org.example.model.exceptions.DAOException;
import org.example.model.entities.Alumne.Matricula;
import org.example.view.ModelComponentsVisuals;
import org.example.model.impls.AlumneDAOJDBCOracleImpl;
import org.example.view.MatriculaView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class Controller implements PropertyChangeListener { //1. Implementació de interfície PropertyChangeListener


    private ModelComponentsVisuals modelComponentsVisuals =new ModelComponentsVisuals();
    private AlumneDAOJDBCOracleImpl dadesAlumnes;
    private MatriculaView view;

    public Controller(AlumneDAOJDBCOracleImpl dadesAlumnes, MatriculaView view) {
        this.dadesAlumnes = dadesAlumnes;
        this.view = view;

        //5. Necessari per a que Controller reaccione davant de canvis a les propietats lligades
        canvis.addPropertyChangeListener(this);

        lligaVistaModel();

        afegirListeners();

        //Si no hem tingut cap poroblema amb la BD, mostrem la finestra
        view.setVisible(true);

    }

    private void lligaVistaModel() {

        //Carreguem la taula d'alumnes en les dades de la BD
        try {
            setModelTaulaAlumne(modelComponentsVisuals.getModelTaulaAlumne(),dadesAlumnes.getAll());
        } catch (DAOException e) {
            this.setExcepcio(e);
        }

            //Fixem el model de la taula dels alumnes
        JTable taula = view.getTaula();
        taula.setModel(this.modelComponentsVisuals.getModelTaulaAlumne());
        //Amago la columna que conté l'objecte alumne
        taula.getColumnModel().getColumn(3).setMinWidth(0);
        taula.getColumnModel().getColumn(3).setMaxWidth(0);
        taula.getColumnModel().getColumn(3).setPreferredWidth(0);

        //Fixem el model de la taula de matrícules
        JTable taulaMat = view.getTaulaMat();
        taulaMat.setModel(this.modelComponentsVisuals.getModelTaulaMat());

        //Posem valor a el combo d'MPs
        view.getComboMP().setModel(modelComponentsVisuals.getComboBoxModel());

        //Desactivem la pestanya de la matrícula
        view.getPestanyes().setEnabledAt(1, false);
        view.getPestanyes().setTitleAt(1, "Matrícula de ...");

        //5. Necessari per a que Controller reaccione davant de canvis a les propietats lligades
        canvis.addPropertyChangeListener(this);
    }

    private void setModelTaulaAlumne(DefaultTableModel modelTaulaAlumne, List<Alumne> all) {

        // Fill the table model with data from the collection
        for (Alumne estudiant : all) {
            modelTaulaAlumne.addRow(new Object[]{estudiant.getNom(), estudiant.getPes(), true, estudiant});
        }
    }

    private void afegirListeners() {

        ModelComponentsVisuals modelo = this.modelComponentsVisuals;
        DefaultTableModel model = modelo.getModelTaulaAlumne();
        DefaultTableModel modelMat = modelo.getModelTaulaMat();
        JTable taula = view.getTaula();
        JTable taulaMat = view.getTaulaMat();
        JButton insertarButton = view.getInsertarButton();
        JButton modificarButton = view.getModificarButton();
        JButton borrarButton = view.getBorrarButton();
        JTextField campNom = view.getCampNom();
        JTextField campPes = view.getCampPes();
        JCheckBox caixaAlumne = view.getCaixaAlumne();
        JTabbedPane pestanyes = view.getPestanyes();

        //Botó insertar
        view.getInsertarButton().addActionListener(
                new ActionListener() {
                    /**
                     * Invoked when an action occurs.
                     *
                     * @param e the event to be processed
                     */
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JTextField campNom = view.getCampNom();
                        JTextField campPes = view.getCampPes();
                        JCheckBox caixaAlumne = view.getCaixaAlumne();

                        if (pestanyes.getSelectedIndex() == 0) {        //Si estem a la pestanya de l'alumne
                            //Comprovem que totes les caselles continguen informació
                            if (campNom.getText().isBlank() || campPes.getText().isBlank()) {
                                JOptionPane.showMessageDialog(null, "Falta omplir alguna dada!!");
                            } else {
                                try {
                                    NumberFormat num = NumberFormat.getNumberInstance(Locale.getDefault());   //Creem un número que entèn la cultura que utilitza l'aplicació
                                    double pes = num.parse(campPes.getText().trim()).doubleValue();  //intentem convertir el text a double
                                    if (pes < 1 || pes > 800) throw new ParseException("", 0);
                                    Alumne al = new Alumne(campNom.getText(), pes, caixaAlumne.isSelected(), new TreeSet<Matricula>());
                                    model.addRow(new Object[]{campNom.getText(), pes, caixaAlumne.isSelected(), al});
                                    campNom.setText("Pepe Gotera Ibáñez");
                                    campNom.setSelectionStart(0);
                                    campNom.setSelectionEnd(campNom.getText().length());
                                    campPes.setText("75");
                                    campNom.requestFocus();         //intentem que el foco vaigue al camp del nom
                                    dadesAlumnes.save(al);
                                } catch (ParseException ex) {
                                    setExcepcio(new DAOException(3));
//                                    JOptionPane.showMessageDialog(null, "Has d'introduir un pes correcte (>=1 i <=800!!");
                                    campPes.setSelectionStart(0);
                                    campPes.setSelectionEnd(campPes.getText().length());
                                    campPes.requestFocus();
                                } catch (DAOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        } else {         //Si estem a la pestanya de la matricula
                            //Obtinc l'alumne de la columna que conté l'objecte
                            Alumne al = (Alumne) model.getValueAt(taula.getSelectedRow(), 3);
                            Matricula m = new Matricula((Matricula.Modul) view.getComboMP().getSelectedItem(), Integer.parseInt(view.getCampNota().getText()));
                            al.getMatricules().add(m);
                            ompliMatricula(al, modelMat);


                        }


                    }
                }
        );

        taula.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                //Obtenim el número de la fila seleccionada
                int filaSel = taula.getSelectedRow();

                if (filaSel != -1) {        //Tenim una fila seleccionada
                    //Posem els valors de la fila seleccionada als camps respectius
                    campNom.setText(model.getValueAt(filaSel, 0).toString());
                    campPes.setText(model.getValueAt(filaSel, 1).toString().replaceAll("\\.", ","));
                    caixaAlumne.setSelected((Boolean) model.getValueAt(filaSel, 2));

                    //Activem la pestanya de la matrícula de l'alumne seleccionat
                    view.getPestanyes().setEnabledAt(1, true);
                    view.getPestanyes().setTitleAt(1, "Matrícula de " + campNom.getText());

                    //Posem valor a el combo d'MPs
                    //view.getComboMP().setModel(modelo.getComboBoxModel());
                    ompliMatricula((Alumne) model.getValueAt(filaSel, 3),modelMat);
                } else {                  //Hem deseleccionat una fila
                    //Posem els camps de text en blanc
                    campNom.setText("");
                    campPes.setText("");

                    //Desactivem pestanyes
                    view.getPestanyes().setEnabledAt(1, false);
                    view.getPestanyes().setTitleAt(1, "Matrícula de ...");
                }
            }
        });

        campNom.addFocusListener(new FocusAdapter() {
            /**
             * Invoked when a component loses the keyboard focus.
             *
             * @param e
             */
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                String regex1="^[A-ZÀ-ÚÑÇ][a-zà-úñç]+\\s+[A-ZÀ-ÚÑÇ][a-zà-úñç]+\\s+[A-ZÀ-ÚÑÇ][a-zà-úñç]+$",
                        regex2="^[A-ZÀ-ÚÑÇ][a-zà-úñç]+(\\s*,\\s*)[A-ZÀ-ÚÑÇ][a-zà-úñç]+\\s+[A-ZÀ-ÚÑÇ][a-zà-úñç]+$";;
                //String regex="[À-ú]";
                //Pattern pattern = Pattern.compile(regex);
                if(campNom.getText().isBlank() || (!campNom.getText().matches(regex1) && !campNom.getText().matches(regex2))){
                    setExcepcio(new DAOException(2));
                }
            }
        });
        //throw new LaMeuaExcepcio(1,"Ha petat la base de dades");
    }



    private static void ompliMatricula(Alumne al,DefaultTableModel modelMat) {
        //Omplim el model de la taula de matrícula de l'alumne seleccionat
        modelMat.setRowCount(0);
        // Fill the table model with data from the collection
        for (Matricula matricula : al.getMatricules()) {
            modelMat.addRow(new Object[]{matricula.getModul(), matricula.getNota()});
        }
    }


    //TRACTAMENT D'EXCEPCIONS

    //2. Propietat lligada per controlar quan genero una excepció
    public static final String PROP_EXCEPCIO="excepcio";
    private DAOException excepcio;

    public DAOException getExcepcio() {
        return excepcio;
    }

    public void setExcepcio(DAOException excepcio) {
        DAOException valorVell=this.excepcio;
        this.excepcio = excepcio;
        canvis.firePropertyChange(PROP_EXCEPCIO, valorVell,excepcio);
    }


    //3. Propietat PropertyChangesupport necessària per poder controlar les propietats lligades
    PropertyChangeSupport canvis=new PropertyChangeSupport(this);


    //4. Mètode on posarem el codi de tractament de les excepcions --> generat per la interfície PropertyChangeListener
    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        DAOException rebuda=(DAOException)evt.getNewValue();

        try {
            throw rebuda;
        } catch (DAOException e) {
            //Aquí farem ele tractament de les excepcions de l'aplicació
            switch(evt.getPropertyName()){
                case PROP_EXCEPCIO:

                    switch(rebuda.getTipo()){
                        case 0:
                            JOptionPane.showMessageDialog(null, rebuda.getMessage());
                            System.exit(1);
                            break;
                        case 1:
                            JOptionPane.showMessageDialog(null, rebuda.getMessage());
                            break;
                        case 2:
                            JOptionPane.showMessageDialog(null, rebuda.getMessage());
                            //this.view.getCampNom().setText(rebuda.getMissatge());
                            this.view.getCampNom().setSelectionStart(0);
                            this.view.getCampNom().setSelectionEnd(this.view.getCampNom().getText().length());
                            this.view.getCampNom().requestFocus();

                            break;
                    }


            }
        }
    }

}
