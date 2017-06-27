/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antalgorithm;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Scanner;

/**
 *
 * @author nicol
 */
public class AntAlgorithm {

    public static int numAnts = 20;
    //public static 
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        int[] vertices = createVertices();
        double[][] distances = addDistances();
        double[][] pheremones = initializePheremones(distances);
        ArrayList<Path> paths = new ArrayList<>();
        
        int time = 0;
        while(time < 20)
        {
            for(int i = 0; i < numAnts; i++)
            {
                Path thisPath = new Path();
                thisPath.path = new ArrayList();
                Boolean hasBeenReached = false;
                int currentPosition = 1;
                int previousPosition = 1;
                int destinationPosition = 5;
                thisPath.path.add(currentPosition);
                BitSet bits = new BitSet(100);
                
                do
                {
                    ArrayList availableNodes = new ArrayList();
                    for(int j = 0; j < distances.length; j++)
                    {
                        if(distances[currentPosition][j] > 0)
                            availableNodes.add(j);
                    }
                    
                    int selectedNode = selectNode(availableNodes, pheremones, 
                            currentPosition, bits, previousPosition);
                    int bitToSet = currentPosition*10 + selectedNode;
                    bits.set(bitToSet);
                    previousPosition = currentPosition;
                    currentPosition = selectedNode;
                    thisPath.path.add(currentPosition);
                    
                    if(currentPosition == destinationPosition)
                        hasBeenReached = true;
                    
                }while(hasBeenReached == false);
                
                removeLoopsFromPath(thisPath.path);
                thisPath.pathLength = calculatePathLength(thisPath.path, distances);
                paths.add(thisPath);
            }
            System.out.println();
            System.out.println("Iteration: " + time);
            int shortestPathIndex = findShortestPath(paths);
            System.out.println("Shortest Path: ");
            System.out.println(paths.get(shortestPathIndex).path.toString());
            reducePheremones(pheremones);
            updatePheremones(pheremones, paths);
            time++;
            
        }
        Scanner stdin = new Scanner(System.in);
        stdin.nextLine();
    }
    
    public static int[] createVertices()
    {
        int[] vertices = new int[10];
        for(int i = 0; i < 10; i++)
        {
            vertices[i] = i;
        }
        return vertices;
    }
    public static double[][] addDistances()
    {
                               //1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        double[][] distances = {{0, 2, 0, 0, 0, 0, 3, 0, 0, 0}, //1
                                {2, 0, 0, 0, 0, 0, 3, 0, 0, 0}, //2
                                {0, 0, 0, 2, 0, 4, 0, 0, 0, 0}, //3
                                {0, 0, 2, 0, 3, 0, 0, 0, 0, 5}, //4
                                {0, 0, 0, 3, 0, 0, 0, 0, 0, 2}, //5
                                {0, 0, 4, 0, 0, 0, 2, 4, 0, 0}, //6
                                {3, 3, 0, 0, 0, 2, 0, 1, 0, 0}, //7
                                {0, 0, 0, 0, 0, 4, 1, 0, 3, 0}, //8
                                {0, 0, 0, 0, 0, 0, 0, 3, 0, 1}, //9
                                {0, 0, 0, 5, 2, 0, 0, 0, 1, 0},}; //10
        
        return distances;
    }
    
    public static double[][] initializePheremones(double[][] distances)
    {
        double[][] pheremones = new double[distances.length][distances.length];
        for(int i = 0; i < distances.length; i++)
        {
            for(int j = 0; j < distances.length; j++)
            {
                if(distances[i][j] == 0)
                {
                    pheremones[i][j] = 0;
                    continue;
                }
                
                if(pheremones[j][i] > 0)
                    pheremones[i][j] = pheremones[j][i];
                else
                    pheremones[i][j] = Math.random();
            }
        }
        return pheremones;
    }
    
    public static double calculatePathLength(ArrayList path, double[][] distances)
    {
        double length = 0;
        for(int i = 0; i < path.size()-1; i++)
        {
            length = length + distances[(int)path.get(i)][(int)path.get(i+1)];
        }
        return length;      
    }
    
    public static int findShortestPath(ArrayList<Path> paths)
    {
        int shortestPathIndex = 0;
        for(int i = 1; i < paths.size(); i++) //i starts at 1 because we already 
        {                                     //have shortestPathIndex set to 0, 
                                              //so we're already getting that index
            if(paths.get(i).pathLength > paths.get(shortestPathIndex).pathLength)
                shortestPathIndex = i;
        }
        return shortestPathIndex;
    }
    
    public static int selectNode(ArrayList availableNodes, double[][] pheremones, 
            int currentPosition, BitSet bits, int previousPosition)
    {
        ArrayList probabilities = new ArrayList();
        double pheremoneSum = 0;

        for(int i = 0; i < pheremones.length; i++)
        {
            if(availableNodes.contains((i)) && !bits.get(currentPosition*10 + i))
            {
                pheremoneSum += pheremones[currentPosition][i];
                probabilities.add(1.0);                                         
            }                             
            else
                probabilities.add(0.0);
        }
        
        for(int i = 0; i < pheremones.length; i++)
        {
            probabilities.set(i, (double)probabilities.get(i) 
                    * (pheremones[currentPosition][i] / pheremoneSum));
        }
        
        int winningPosition = 0;
        for(int i = 0; i < probabilities.size(); i++)
        {
            if((double)probabilities.get(i) > (double)probabilities.get(winningPosition))
                winningPosition = i;
        }
        if((double)probabilities.get(winningPosition) <= 0)
            return previousPosition;
        return winningPosition;
    }
    
    public static void removeLoopsFromPath(ArrayList path)
    {
        for(int i = 0; i < path.size(); i++)
        {
            int firstLocation = path.indexOf(path.get(i));
            int lastLocation = path.lastIndexOf(path.get(i));
            if(firstLocation != lastLocation)
            {
                for(int j = lastLocation; j > firstLocation; j--)
                    path.remove(j);
            }
        }
    }
    
    public static void reducePheremones(double[][] pheremones)
    {
        double p = 0.3;
        for(int i = 0; i < pheremones.length; i++)
        {
            for(int j = 0; j < pheremones[0].length; j++)
            {
                pheremones[i][j] = (1.0 - p)*pheremones[i][j];
            }
        }
    }
    
    public static void updatePheremones(double[][] pheremones, ArrayList<Path> paths)
    {
        double sumOfDeltas = 0;
        for(int n = 0; n < paths.size(); n++)
        {
            double delta = 1.0/paths.get(n).pathLength;
            sumOfDeltas += delta;
        }
        
        for(int a = 0; a < numAnts; a++)
        {
            Path thisPath = paths.get(a);
            for(int i = 0; i < thisPath.path.size() - 1; i++)
            {
                
                pheremones[(int)(thisPath.path.get(i))][(int)(thisPath.path.get(i+1))] 
                        = pheremones[(int)(thisPath.path.get(i))][(int)(thisPath.path.get(i+1))] + sumOfDeltas;
            }
        }
    }
}
