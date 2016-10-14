package DAO;

import Interfaces.iEmpleadoDAO;
import Modelo.Empleado;
import Modelo.TipoDocumento;
import Modelo.TipoEmpleado;
import Util.Conexion;
import java.util.List;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Logger;

public class EmpleadoDAO implements iEmpleadoDAO{

    private static Logger logger = Logger.getLogger(TipoServicioDAO.class.getName());
    private Conexion con;
    private Connection cn;
    private ResultSet rs;
    private PreparedStatement ps;
    private CallableStatement cs;
    private int flgOperacion = 0;
    private String sql;
    
    @Override
    public int insertar(Empleado empleado) {
    logger.info("Insertando Empleado");
        sql= "{CALL P_Insertar_Empleado(?,?,?,?,?,?,?,?,?,?)}";
        try{
            con=new Conexion();
            cn=con.getConexion();
            cn.setAutoCommit(false);
            cs = cn.prepareCall(sql.trim());
            cs.setInt(1, empleado.getTipoDocumento().getIdTipoDocumento());
            cs.setString(2, empleado.getTipoDocumento().getDescripcion());
            cs.setInt(3, empleado.getIdPersona());
            cs.setString(4, empleado.getTipoEmpleado().getIdTipoEmpleado());
            cs.setString(5, empleado.getNumeroDocumento().trim());
            cs.setString(6, empleado.getNombres().trim());
            cs.setString(7, empleado.getDireccion().trim());
            cs.setString(8, empleado.getTelefono().trim());
            cs.setString(9, empleado.getEmail().trim());
            cs.registerOutParameter(10, java.sql.Types.INTEGER);
            cs.executeUpdate();
            flgOperacion = Integer.parseInt(cs.getObject(10).toString());
            if(flgOperacion==1){
                cn.commit();
            }else{
                cn.rollback();
            }
        }catch(Exception e){
            logger.info("Error al insertar" + e.getMessage());
        }finally{
            con.cerrarConexion(cn);
        }
        return flgOperacion;    
    }

    @Override
    public List<Empleado> buscar(String nombres, int inicio, int registrosPorPagina) {
        logger.info("buscar");
        sql = "select idEmpleado,p.idpersona, p.nombres, td.descripcion, p.numerodocumento, p.email, p.direccion, p.telefono, te.descripcion "
                + "from tipoempleado as te inner join empleado as e " +
                "on te.idtipoempleado=e.idtipoempleado " +
                "inner join persona as p " +
                "on e.idpersona=p.idpersona inner join tipodocumento as td " +
                "on p.idtipodocumento=td.idtipodocumento "
                + "where nombres like '%" + (nombres.trim()) + "%' "
                + "order by idEmpleado desc LIMIT " + inicio + ", " + registrosPorPagina;
        
        List<Empleado> lstEmpleado = null;
        Empleado empleado;
        TipoDocumento tipoDocumento;
        TipoEmpleado tipoEmpleado;
        try{
            con = new Conexion();
            cn = con.getConexion();
            cn.setAutoCommit(false);
            ps = cn.prepareStatement(sql);
            rs = ps.executeQuery();
            lstEmpleado = new ArrayList<Empleado>();
            while(rs.next()){
                //Todos los campos que vamos a mostrar de la consulta
                empleado = new Empleado();
                tipoEmpleado = new TipoEmpleado();
                tipoDocumento = new TipoDocumento();
                
                empleado.setIdEmpleado(rs.getInt("idEmpleado"));
                empleado.setIdPersona(rs.getInt("idpersona"));
                empleado.setNombres(rs.getString("nombres"));
                
                tipoDocumento.setDescripcion(rs.getString("td.descripcion"));
                empleado.setTipoDocumento(tipoDocumento);
                
                empleado.setNumeroDocumento(rs.getString("numerodocumento")); 
                empleado.setEmail(rs.getString("email"));
                empleado.setDireccion(rs.getString("direccion")); 
                empleado.setTelefono(rs.getString("telefono"));              
                
                tipoEmpleado.setDescripcion(rs.getString("te.descripcion"));
                
                empleado.setTipoEmpleado(tipoEmpleado);

                lstEmpleado.add(empleado);
            }
        }catch(Exception e){
            logger.info("Error buscar: " + e.getMessage());
        }finally{
            con.cerrarConexion(cn);
        }
        return lstEmpleado;
    }

    @Override
    public int totalRegistros(String nombres, int inicio, int registrosPorPagina){
        int total = 0;
        logger.info("Total de Registros");
        //cuenta total de registros que existen con el mismo nombre
        sql = "select count(*) as total "
                + "from empleado e " +
                    "inner join persona p " +
                    "on e.idPersona=p.idPersona "
                + "where nombres like '%" + (nombres.trim()) + "%'";
        try{
            con = new Conexion();
            cn = con.getConexion();
            cn.setAutoCommit(false);
            ps = cn.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()){
                total = rs.getInt("total");
            }
        }catch(Exception e){
            logger.info("Error en Total de Registros: " + e.getMessage());
        }finally{
            con.cerrarConexion(cn);
        }
        return total;
    }

    @Override
    public Empleado obtenerPorId(int id) {
        logger.info("buscarPorId");
        sql = "select idEmpleado,p.idpersona, p.nombres, td.descripcion, p.numerodocumento, p.direccion, p.telefono, te.idtipoempleado, te.descripcion "
                + "from tipoempleado as te inner join empleado as e " +
                "on te.idtipoempleado=e.idtipoempleado " +
                "inner join persona as p " +
                "on e.idpersona=p.idpersona inner join tipodocumento as td " +
                "on p.idtipodocumento=td.idtipodocumento"
                + "where idEmpleado = ?";
        Empleado empleado = null;
        TipoEmpleado tipoEmpleado = null;
        TipoDocumento tipoDocumento =  null;
        try{
            con = new Conexion();
            cn = con.getConexion();
            cn.setAutoCommit(false);
            ps = cn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            while(rs.next()){
                empleado = new Empleado();
                tipoEmpleado =  new TipoEmpleado();
                tipoDocumento = new TipoDocumento();
                //Todos los campos del empleado que se actualizara por ID
                empleado.setIdEmpleado(rs.getInt("idEmpleado"));
                empleado.setIdPersona(rs.getInt("idpersona"));
                empleado.setNombres(rs.getString("nombres")); 
                
                tipoDocumento.setDescripcion(rs.getString("td.descripcion"));
                empleado.setTipoDocumento(tipoDocumento);
                
                empleado.setNumeroDocumento(rs.getString("numerodocumento")); 
                empleado.setDireccion(rs.getString("direccion")); 
                empleado.setTelefono(rs.getString("telefono"));
                
                tipoEmpleado.setIdTipoEmpleado(rs.getString("idtipoempleado"));
                tipoEmpleado.setDescripcion(rs.getString("te.descripcion"));
                empleado.setTipoEmpleado(tipoEmpleado);
                
            }
        }catch(Exception e){
            logger.info("buscarPorId: " + e.getMessage());
        }finally{
            con.cerrarConexion(cn);
        }
        return empleado;
    }

    @Override
    public int actualizar(Empleado empleado) {
        logger.info("actualizar");
        sql = "{CALL P_Actualizar_Empleado(?,?,?)}";
        try{
            con = new Conexion();
            cn = con.getConexion();
            cn.setAutoCommit(false);
            cs = cn.prepareCall(sql.trim());
            cs.setInt(1, empleado.getTipoDocumento().getIdTipoDocumento());
            cs.setString(2, empleado.getTipoDocumento().getDescripcion());
            cs.setInt(3, empleado.getIdPersona());
            cs.setString(4, empleado.getTipoEmpleado().getIdTipoEmpleado());
            cs.setString(5, empleado.getNumeroDocumento().trim());
            cs.setString(6, empleado.getNombres().trim());
            cs.setString(7, empleado.getDireccion().trim());
            cs.setString(8, empleado.getTelefono().trim());
            cs.setString(9, empleado.getEmail().trim());
            cs.registerOutParameter(10, java.sql.Types.INTEGER);
            cs.executeUpdate();
            flgOperacion = Integer.parseInt(cs.getObject(10).toString());
            if(flgOperacion == 1){
                cn.commit();
            }else{
                cn.rollback();
            }
        }catch(Exception e){
            logger.info("Eror al actualizar: " + e.getMessage() + " --> "+empleado.getIdEmpleado());
        }finally{
            con.cerrarConexion(cn);
        }
        return flgOperacion;
    }

    @Override
    public int eliminar(int id) {
        logger.info("Eliminar Empleado");
        sql= "DELETE FROM Empleado where idEmpleado = ?";
        try{
            con=new Conexion();
            cn=con.getConexion();
            cn.setAutoCommit(false);
            ps=cn.prepareStatement(sql);
            ps.setInt(1, id);
            flgOperacion=ps.executeUpdate();
            if(flgOperacion>0){
                cn.commit();
            }else{
                cn.rollback();
            }
        }catch(Exception e){
            logger.info("Error al Eliminar" + e.getMessage());
        }finally{
            con.cerrarConexion(cn);
        }
        return flgOperacion;
    }
}