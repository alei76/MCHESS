package utilities.dataobjects.sensorobjects;

import utilities.dataobjects.Dataset;
import utilities.dataobjects.Record;
import utilities.machinelearning.baseobjects.Cluster;
import utilities.machinelearning.baseobjects.Clusters;
import utilities.machinelearning.clusters.AffinityPropagation.BaseAffinityPropagation;
import utilities.machinelearning.clusters.AffinityPropagation.DensityAffinityPropagation;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by YaHung on 2015/9/15.
 */

public class Sensor implements Serializable, Iterable<BaseSensor> {
    protected boolean used;
    protected int id;
    protected ArrayList<BaseSensor> instances = new ArrayList<>();
    protected String filePath;
    protected DensityAffinityPropagation dap;
    protected int roomId;
    protected int applianceId;

    public Sensor(int id){
        used = false;
        this.id = id;
        this.roomId = -1;
        this.applianceId = -1;
        checkDirectory();
    }

    public Sensor(int id, int roomId){
        used = false;
        this.id = id;
        this.roomId = roomId;
        this.applianceId = -1;
        checkDirectory();
    }

    public Sensor(int id, int roomId, int applianceId){
        used = false;
        this.id = id;
        this.roomId = roomId;
        this.applianceId = applianceId;
        checkDirectory();
    }

    private void checkDirectory(){
        filePath = "util";
        File file = new File(filePath);
        if(!file.exists()) { file.mkdir();}

        filePath += "/dataobjects";
        file = new File(filePath);
        if(!file.exists()) { file.mkdir();}

        filePath += "/sensorobjects";
        file = new File(filePath);
        if(!file.exists()) { file.mkdir();}
    }

    public void add(double value){
        used = true;
        instances.add(new BaseSensor(value));
    }

    public void add(double value, Timestamp timestamp) {
        used = true;
        instances.add(new BaseSensor(value, timestamp));
    }

    public boolean checkUsed(){
        return used;
    }

    public int getNumberInstances(){
        return instances.size();
    }

    public double getValue(int iId){
        return instances.get(iId).getValue();
    }

    public Timestamp getTimestamp(int iId){
        return instances.get(iId).getTimestamp();
    }

    public void buildClusters(){
        if(used) {
            Dataset db = new Dataset();
            for (BaseSensor instance : instances) {
                double sensorValue = instance.getValue();
                Record record = new Record(new Double[]{sensorValue},0);
                db.add(record);
            }
            this.dap = new DensityAffinityPropagation();
            dap.setDataset(db);
            dap.run();

            System.out.println("Sensor " + id + ", number of clusters of this sensor is " + dap.getClusters().getNumberOfCluster());

        }
        else{
            System.out.println("Sensor "+id+" is not used.");
        }
    }

    public Integer predict(double value){
        int result = (Integer)dap.predict(new Double[]{value},0);
        return result;
    }

    public int getId(){
        return id;
    }

    private String getFilePath(){
        return filePath;
    }

    public ArrayList<BaseSensor> getInstances(){
        return instances;
    }

    public Clusters getClusterList(){
        return dap.getClusters();
    }

    public int getRoomId(){return roomId;}

    public int getApplianceId(){return applianceId;}

    public void save(){
        try {
            FileOutputStream fos = new FileOutputStream(filePath+"/"+id);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.flush();
            oos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(){
        try {
            FileInputStream fis = new FileInputStream(filePath+"/"+id);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Sensor s = (Sensor) ois.readObject();
            ois.close();
            this.filePath = s.getFilePath();
            this.instances = s.getInstances();
            this.dap.setClusters(s.getClusterList());
            this.roomId = s.getRoomId();
            this.applianceId = s.applianceId;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Iterator<BaseSensor> iterator() {
        return new Iterator<BaseSensor>() {
            private Integer counter = 0;
            private final int n = instances.size();

            @Override
            public boolean hasNext() {
                return counter<n;
            }

            @Override
            public BaseSensor next() {
                return instances.get(counter++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


}
