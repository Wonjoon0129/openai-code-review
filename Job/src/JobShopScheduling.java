import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
// Represents an individual operation in a job
class Operation {
    int jobId;//工序所属job
    int operationIndex;
    int machineId;//处理工序的机器号
    int processingTime;//处理时间
    int startTime;//开始处理时间
    int endTime;//结束时间

    public Operation(int jobId, int operationIndex, int machineId, int processingTime) {
        this.jobId = jobId;
        this.operationIndex = operationIndex;
        this.machineId = machineId;
        this.processingTime = processingTime;
        this.startTime = -1;
        this.endTime = -1;
    }

    @Override
    public String toString() {
        return "(Job " + jobId + ", Op " + operationIndex + ", Machine " + machineId + ", Start " + startTime + ", End " + endTime + ")";
    }
}

// Represents a single job with multiple operations
class Job {
    int jobId;
    List<Operation> operations;
    int current_operation;//当前处理的工序序号
    public Job(int jobId) {
        this.current_operation=0;
        this.jobId = jobId;
        this.operations = new ArrayList<>();
    }

    public void addOperation(Operation operation) {
        this.operations.add(operation);
    }

    public Job deepCopy() {
        Job copy = new Job(this.jobId);
        copy.current_operation = this.current_operation;
        for (Operation op : this.operations) {
            copy.operations.add(new Operation(
                    op.jobId,
                    op.operationIndex,
                    op.machineId,
                    op.processingTime
            ));
        }
        return copy;
    }
}

// Represents a machine
class Machine {
    int machineId;
    int availableTime;//机器可供使用的时间点

    int processing_job;//机器将要处理的job序号

    public Machine(int machineId) {
        this.machineId = machineId;
        this.availableTime = 0;
        this.processing_job=-1;
    }
}

// KK Scheduling Algorithm
class Algorithm {
    List<Job> jobs;
    List<Machine> machines;
    private List<String> scheduleOrder = new ArrayList<>(); // 记录工序安排顺序

    public String getScheduleOrder() {
        return String.join(" ", scheduleOrder);
    }
    public void resetState() {
        // 重置作业状态
        for (Job job : this.jobs) {
            job.current_operation = 0;
            for (Operation op : job.operations) {
                op.startTime = -1;
                op.endTime = -1;
            }
        }
        for (Machine machine : this.machines) {
            machine.availableTime = 0;
            machine.processing_job = -1;
        }

        this.scheduleOrder.clear();
    }

    public Algorithm(List<Job> jobs, List<Machine> machines) {
        this.jobs = jobs;
        this.machines = machines;
    }
    private int calculateRemainingTime(Job job) {
        if (job.current_operation == -1) return 0;
        int remaining = 0;
        for (int i = job.current_operation; i < job.operations.size(); i++) {
            remaining += job.operations.get(i).processingTime;
        }
        return remaining;
    }

    // 最短剩余时间优先算法
    public int SRTF() {
        int last_time = 0;
        List<Integer> occupy_judge = new ArrayList<>();
        List<Integer> min_search = new ArrayList<>();

        for (int i = 0; i < JobShopScheduling.JobShopData.numMachines; i++) {
            occupy_judge.add(0);
        }

        while (true) {
            int sum_judge = 0;
            for (int i = 0; i < JobShopScheduling.JobShopData.numJobs; i++) {
                if (jobs.get(i).current_operation == -1) sum_judge++;
            }
            if (sum_judge == JobShopScheduling.JobShopData.numJobs) break;

            // 重置机器占用状态
            for (int i = 0; i < JobShopScheduling.JobShopData.numMachines; i++) {
                occupy_judge.set(i, machines.get(i).availableTime <= last_time ? 1 : 0);
            }

            // 选择工序逻辑
            for (Job job : jobs) {
                if (job.current_operation == -1) continue;
                Operation currentOp = job.operations.get(job.current_operation);
                int machineId = currentOp.machineId;

                if (occupy_judge.get(machineId) == 1) {
                    int currentRemaining = calculateRemainingTime(job);
                    int existingJobId = machines.get(machineId).processing_job;

                    if (existingJobId == -1 || currentRemaining < calculateRemainingTime(jobs.get(existingJobId))) {
                        machines.get(machineId).processing_job = job.jobId;
                    }
                }
            }

            // 处理选中的工序
            for (int i = 0; i < JobShopScheduling.JobShopData.numMachines; i++) {
                if (occupy_judge.get(i) == 1 && machines.get(i).processing_job != -1) {
                    Job currentJob = jobs.get(machines.get(i).processing_job);
                    Operation currentOp = currentJob.operations.get(currentJob.current_operation);

                    // 更新时间和状态
                    currentOp.startTime = last_time;
                    currentOp.endTime = last_time + currentOp.processingTime;
                    machines.get(i).availableTime = currentOp.endTime;

                    // 更新作业状态
                    if (currentJob.current_operation < currentJob.operations.size() - 1) {
                        currentJob.current_operation++;
                    } else {
                        currentJob.current_operation = -1;
                    }
                    scheduleOrder.add(String.valueOf(currentJob.jobId));
                }
            }

            // 重置机器处理作业状态
            for (int i = 0; i < JobShopScheduling.JobShopData.numMachines; i++) {
                machines.get(i).processing_job = -1;
            }

            // 推进时间
            min_search.clear();
            for (Machine machine : machines) {
                if (machine.availableTime > last_time) {
                    min_search.add(machine.availableTime);
                }
            }
            last_time = Collections.min(min_search);
        }

        // 计算最大完成时间
        return jobs.stream()
                .mapToInt(job -> job.operations.get(job.operations.size()-1).endTime)
                .max().orElse(0);
    }

    // 最长剩余时间优先算法（与SRTF对称实现）
    public int LRTF() {
        int last_time = 0;
        List<Integer> occupy_judge = new ArrayList<>();
        List<Integer> min_search = new ArrayList<>();

        for (int i = 0; i < JobShopScheduling.JobShopData.numMachines; i++) {
            occupy_judge.add(0);
        }

        while (true) {
            int sum_judge = 0;
            for (int i = 0; i < JobShopScheduling.JobShopData.numJobs; i++) {
                if (jobs.get(i).current_operation == -1) sum_judge++;
            }
            if (sum_judge == JobShopScheduling.JobShopData.numJobs) break;

            // 重置机器占用状态
            for (int i = 0; i < JobShopScheduling.JobShopData.numMachines; i++) {
                occupy_judge.set(i, machines.get(i).availableTime <= last_time ? 1 : 0);
            }


            // 不同点：选择剩余时间更长的作业
            for (Job job : jobs) {
                if (job.current_operation == -1) continue;
                Operation currentOp = job.operations.get(job.current_operation);
                int machineId = currentOp.machineId;

                if (occupy_judge.get(machineId) == 1) {
                    int currentRemaining = calculateRemainingTime(job);
                    int existingJobId = machines.get(machineId).processing_job;

                    // 修改比较条件为大于
                    if (existingJobId == -1 || currentRemaining > calculateRemainingTime(jobs.get(existingJobId))) {
                        machines.get(machineId).processing_job = job.jobId;
                    }
                }
            }
            // 处理选中的工序
            for (int i = 0; i < JobShopScheduling.JobShopData.numMachines; i++) {
                if (occupy_judge.get(i) == 1 && machines.get(i).processing_job != -1) {
                    Job currentJob = jobs.get(machines.get(i).processing_job);
                    Operation currentOp = currentJob.operations.get(currentJob.current_operation);

                    // 更新时间和状态
                    currentOp.startTime = last_time;
                    currentOp.endTime = last_time + currentOp.processingTime;
                    machines.get(i).availableTime = currentOp.endTime;

                    // 更新作业状态
                    if (currentJob.current_operation < currentJob.operations.size() - 1) {
                        currentJob.current_operation++;
                    } else {
                        currentJob.current_operation = -1;
                    }
                    scheduleOrder.add(String.valueOf(currentJob.jobId));
                }
            }

            // 重置机器处理作业状态
            for (int i = 0; i < JobShopScheduling.JobShopData.numMachines; i++) {
                machines.get(i).processing_job = -1;
            }

            // 推进时间
            min_search.clear();
            for (Machine machine : machines) {
                if (machine.availableTime > last_time) {
                    min_search.add(machine.availableTime);
                }
            }
            last_time = Collections.min(min_search);
        }

        // 计算最大完成时间
        return jobs.stream()
                .mapToInt(job -> job.operations.get(job.operations.size()-1).endTime)
                .max().orElse(0);
    }

    private int schedulingAlgorithm(boolean isShortest) {
        int last_time = 0;

        List<Integer> occupy_judge = new ArrayList<>();
        List<Integer> min_search = new ArrayList<>();
        for (int i = 0; i < JobShopScheduling.JobShopData.numMachines; i++)
            occupy_judge.add(0); // 用于统计某一时刻机器是否被占用

        while (true) {
            int sum_judge = 0;
            for (int i = 0; i < JobShopScheduling.JobShopData.numJobs; i++)
                if (jobs.get(i).current_operation == -1)
                    sum_judge++;
            if (sum_judge == JobShopScheduling.JobShopData.numJobs) // 用于统计处理完的作业数目，处理序号为-1代表处理完毕
                break;

            // 重置机器占用状态
            for (int i = 0; i < JobShopScheduling.JobShopData.numMachines; i++)
                occupy_judge.set(i, machines.get(i).availableTime <= last_time ? 1 : 0);

            for (Job job : jobs) { // 机器获得这一轮可以处理的工序
                if (job.current_operation == -1)
                    continue; // 跳过处理完的工序
                Operation currentOp = job.operations.get(job.current_operation);
                int machineId = currentOp.machineId;

                if (occupy_judge.get(machineId) == 1) { // 判定该工序能否在该时刻被处理
                    int currentProcessingTime = currentOp.processingTime;
                    int existingJobId = machines.get(machineId).processing_job;
                    if (existingJobId == -1 || (isShortest ? currentProcessingTime < jobs.get(existingJobId).operations.get(jobs.get(existingJobId).current_operation).processingTime
                            : currentProcessingTime > jobs.get(existingJobId).operations.get(jobs.get(existingJobId).current_operation).processingTime)) {
                        machines.get(machineId).processing_job = job.jobId;
                    }
                }
            }

            for (int i = 0; i < JobShopScheduling.JobShopData.numMachines; i++) { // 处理工序
                if (occupy_judge.get(i) == 1 && machines.get(i).processing_job != -1) {
                    Job currentJob = jobs.get(machines.get(i).processing_job);
                    Operation currentOp = currentJob.operations.get(currentJob.current_operation);

                    // 更新时间和状态
                    currentOp.startTime = Math.max(last_time, machines.get(i).availableTime);
                    currentOp.endTime = currentOp.startTime + currentOp.processingTime;
                    machines.get(i).availableTime = currentOp.endTime;

                    // 更新作业状态
                    if (currentJob.current_operation < currentJob.operations.size() - 1)
                        currentJob.current_operation++;
                    else
                        currentJob.current_operation = -1;
                    scheduleOrder.add(String.valueOf(currentJob.jobId));
                }
            }

            // 重置机器处理作业状态和占用状态
            for (int i = 0; i < JobShopScheduling.JobShopData.numMachines; i++) {
                machines.get(i).processing_job = -1; // 重置
                occupy_judge.set(i, 0); // 重置
            }

            // 获得下一个处理时刻
            min_search.clear();
            for (int i = 0; i < JobShopScheduling.JobShopData.numMachines; i++) {
                if (machines.get(i).availableTime > last_time)
                    min_search.add(machines.get(i).availableTime);
            }
            if (!min_search.isEmpty()) {
                last_time = Collections.min(min_search);
            } else {
                break;
            }
            min_search.clear();
        }

        // 返回总加工时间
        return jobs.stream()
                .mapToInt(job -> job.operations.get(job.operations.size()-1).endTime)
                .max()
                .orElse(0);
    }

    public int LPT() {
        return schedulingAlgorithm(false);
    }

    public int SPT() {
        return schedulingAlgorithm(true);
    }

    public int KK()
    {
        int last_time = 0; // 当前时间

        // Step 1: 初始化分配第一步的工序
        List<Operation> initialOperations = new ArrayList<>();
        for (Job job : jobs) {
            if (!job.operations.isEmpty()) {
                initialOperations.add(job.operations.get(0)); // 添加每个作业的第一个工序
            }
        }

        // 按加工时间降序排序，如果相同则随机选择
        initialOperations.sort((o1, o2) -> Integer.compare(o2.processingTime, o1.processingTime));

        for (Operation operation : initialOperations) {
            Machine machine = machines.get(operation.machineId);
            operation.startTime = machine.availableTime;
            operation.endTime = operation.startTime + operation.processingTime;
            machine.availableTime = operation.endTime;
            jobs.get(operation.jobId).current_operation = 1; // 标记下一个工序
            scheduleOrder.add(String.valueOf(operation.jobId));
        }

        // Step 2 & Step 3: 依次分配后续工序
        while (true) {
            boolean allJobsCompleted = true;

            // 检查是否所有作业完成
            for (Job job : jobs) {
                if (job.current_operation != -1) { // 当前作业还有未完成的工序
                    allJobsCompleted = false;
                    break;
                }
            }
            if (allJobsCompleted) {
                break; // 所有作业完成，退出循环
            }

            // 收集所有可分配的工序
            List<Operation> candidateOperations = new ArrayList<>();
            for (Job job : jobs) {
                int currentIndex = job.current_operation;
                if (currentIndex != -1 && currentIndex < job.operations.size()) {
                    candidateOperations.add(job.operations.get(currentIndex));
                }
            }

            // 按前置工序完成时间排序，如果相同则随机选择
            candidateOperations.sort((o1, o2) -> {
                int finishTime1 = jobs.get(o1.jobId).operations.get(o1.operationIndex - 1).endTime;
                int finishTime2 = jobs.get(o2.jobId).operations.get(o2.operationIndex - 1).endTime;
                return Integer.compare(finishTime1, finishTime2); // 按前置工序完成时间升序
            });

            // 分配工序到机器
            for (Operation operation : candidateOperations) {
                Machine machine = machines.get(operation.machineId);
                Job job = jobs.get(operation.jobId);

                // 如果机器空闲，分配操作
                if (machine.availableTime <= last_time) {
                    operation.startTime = Math.max(machine.availableTime,
                            job.operations.get(operation.operationIndex - 1).endTime);
                    operation.endTime = operation.startTime + operation.processingTime;
                    machine.availableTime = operation.endTime;

                    // 更新作业状态
                    if (job.current_operation < job.operations.size() - 1) { // 还有下一步
                        job.current_operation++;
                    } else { // 所有步骤完成
                        job.current_operation = -1;
                    }
                    scheduleOrder.add(String.valueOf(operation.jobId)); // 记录加工顺序
                }
            }

            // 推进时间到下一个可用时间点
            int nextTime = Integer.MAX_VALUE;
            for (Machine machine : machines) {
                if (machine.availableTime > last_time) {
                    nextTime = Math.min(nextTime, machine.availableTime);
                }
            }
            if (nextTime == Integer.MAX_VALUE) {
                nextTime = last_time + 1;
            }
            last_time = nextTime;

        }

        // 返回总加工时间
        return machines.stream()
                .mapToInt(machine -> machine.availableTime)
                .max()
                .orElse(0);
    }
}


class GanttChartPrinter extends JPanel {
    private final int makespan; // 添加makespan字段
    private List<Job> jobs;
    private int numMachines;
    private final Map<Integer, Color> jobColors = new HashMap<>(); // 存储作业颜色的映射
    // 修改构造函数以接收makespan
    public GanttChartPrinter(List<Job> jobs, int numMachines, int makespan) {
        this.jobs = jobs;
        this.numMachines = numMachines;
        this.makespan = makespan;
        initializeJobColors();
    }
    // Constructor
    public GanttChartPrinter(List<Job> jobs, int numMachines) {
        this.jobs = jobs;
        this.numMachines = numMachines;
        initializeJobColors(); // 在构造函数中初始化颜色
        makespan = 0;
    }

    // 初始化作业颜色
    private void initializeJobColors() {
        Random random = new Random(123); // 使用固定种子保证颜色一致性
        for (Job job : jobs) {
            jobColors.put(job.jobId, new Color(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
            ));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Constants for drawing
        int timeUnitWidth = 1; // 每个时间单位100像素
        int margin = 80; // 增大左边距
        int machineHeight = 50; // 增加机器行高
        int fontSize = 12; // 增大字体
        int maxTime = makespan;
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int panelWidth = margin + maxTime * timeUnitWidth + 200; // 右侧留200像素给图例
        int panelHeight = margin + numMachines * machineHeight + 50;
        this.setPreferredSize(new Dimension(panelWidth, panelHeight));
        // 在顶部中央显示makespan
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize + 2));

        // 绘制时间刻度（仅显示100的倍数）
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSize));
        List<Integer> ticks = new ArrayList<>();
        for (int t = 0; t <= maxTime; t += 100) {
            ticks.add(t);
        }
        if (!ticks.contains(maxTime)) {
            ticks.add(maxTime);
        }
        for (int t : ticks) {
            int x = margin + t * timeUnitWidth;
            g2d.drawLine(x, margin - 5, x, margin);
            g2d.drawString(String.valueOf(t), x - 15, margin - 10);
        }

        // 绘制时间刻度（仅显示100的倍数）
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSize));
        for (int t = 0; t <= makespan; t += 100) {
            int x = margin + t * timeUnitWidth;
            g2d.drawLine(x, margin - 5, x, margin);
            g2d.drawString(String.valueOf(t), x - 15, margin - 10);
        }
        // Calculate maximum time from all operations
        for (Job job : jobs) {
            for (Operation operation : job.operations) {
                if (operation.endTime > maxTime) {
                    maxTime = operation.endTime;
                }
            }
        }

        // Prepare operations grouped by machines
        List<List<Operation>> machineSchedules = new ArrayList<>();
        for (int i = 0; i < numMachines; i++) {
            machineSchedules.add(new ArrayList<>());
        }

        // Collect operations per machine
        for (Job job : jobs) {
            for (Operation operation : job.operations) {
                if (operation.startTime >= 0) {
                    machineSchedules.get(operation.machineId).add(operation);
                }
            }
        }

        // Sort operations by start time for each machine
        for (List<Operation> machineSchedule : machineSchedules) {
            machineSchedule.sort(Comparator.comparingInt(o -> o.startTime));
        }

        // Draw each machine's schedule
        for (int machineId = 0; machineId < numMachines; machineId++) {
            int yPosition = margin + machineId * machineHeight; // Y position for this machine

            // Draw machine label
            g2d.drawString("Machine " + machineId, 10, yPosition + machineHeight / 2);

            // Draw operations and idle time
            List<Operation> schedule = machineSchedules.get(machineId);
            int lastEndTime = 0; // Track the end time of the last operation

            for (Operation operation : schedule) {
                // Draw idle time (if any)
                if (operation.startTime > lastEndTime) {
                    int idleWidth = (operation.startTime - lastEndTime) * timeUnitWidth;
                    g2d.setColor(Color.LIGHT_GRAY); // Use light gray for idle time
                    g2d.fillRect(margin + lastEndTime * timeUnitWidth, yPosition + 10, idleWidth, machineHeight - 20);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(margin + lastEndTime * timeUnitWidth, yPosition + 10, idleWidth, machineHeight - 20);
                }

                // Draw task rectangle
                int taskWidth = (operation.endTime - operation.startTime) * timeUnitWidth;
                int taskHeight = machineHeight - 20;
                int xPosition = margin + operation.startTime * timeUnitWidth;

                // Set color based on job ID (using pre-initialized colors)
                g2d.setColor(jobColors.get(operation.jobId));
                g2d.fillRect(xPosition, yPosition + 10, taskWidth, taskHeight);

                // Draw task border
                g2d.setColor(Color.BLACK);
                g2d.drawRect(xPosition, yPosition + 10, taskWidth, taskHeight);

                // Update lastEndTime
                lastEndTime = operation.endTime;
            }

            // Draw idle time after the last operation (if any)
            if (lastEndTime < maxTime) {
                int idleWidth = (maxTime - lastEndTime) * timeUnitWidth;
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillRect(margin + lastEndTime * timeUnitWidth, yPosition + 10, idleWidth, machineHeight - 20);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(margin + lastEndTime * timeUnitWidth, yPosition + 10, idleWidth, machineHeight - 20);
            }
        }

        // 右侧垂直图例
        int legendX = margin + maxTime * timeUnitWidth + 20;
        int legendY = margin;
        int legendItemHeight = 25;

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSize));
        for (Map.Entry<Integer, Color> entry : jobColors.entrySet()) {
            int jobId = entry.getKey();
            Color color = entry.getValue();

            // 绘制颜色块
            g2d.setColor(color);
            g2d.fillRect(legendX, legendY, 20, 15);

            // 绘制文字
            g2d.setColor(Color.BLACK);
            g2d.drawString("Job " + jobId, legendX + 25, legendY + 12);

            legendY += legendItemHeight;

            // 换列（每列15个）
            if ((legendY - margin) / legendItemHeight % 15 == 0) {
                legendX += 120;
                legendY = margin;
            }
        }
    }
}


public class JobShopScheduling {
    public static void main(String[] args) {
        try {
            Crossover();
            showScheduleGantt("schedule.txt", "input.txt");
            int makespan1 = ScheduleDecoder.decodeFromFile("schedule.txt", "input.txt");
            System.out.println("File-based makespan: " + makespan1);

        } catch (IOException e) {
            System.err.println("发生IO异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void showScheduleGantt(String scheduleFile, String inputFile) throws IOException {
        // 读取数据并计算时间
        JobShopData data = readJobShopFile(inputFile);
        int makespan = ScheduleDecoder.decodeFromFile(scheduleFile, inputFile);

        // 读取调度顺序
        List<String> scheduleOrder = Files.readAllLines(Paths.get(scheduleFile)).stream()
                .flatMap(line -> Arrays.stream(line.split(" ")))
                .collect(Collectors.toList());

        // 根据调度顺序更新作业的startTime和endTime
        List<Job> updatedJobs = updateJobsWithScheduleOrder(data.jobs, scheduleOrder);

        // 创建甘特图窗口
        JFrame frame = new JFrame("Custom Schedule - Makespan: " + makespan);
        GanttChartPrinter chart = new GanttChartPrinter(updatedJobs, JobShopData.numMachines, makespan);

        // 添加滚动条
        JScrollPane scrollPane = new JScrollPane(chart);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // 设置窗口属性
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.add(scrollPane);
        frame.setVisible(true);
    }

    private static List<Job> updateJobsWithScheduleOrder(List<Job> jobs, List<String> scheduleOrder) {
        // 初始化作业状态
        Map<Integer, JobState> jobStates = new HashMap<>();
        for (Job job : jobs) {
            jobStates.put(job.jobId, new JobState(job));
        }

        // 初始化机器状态
        Map<Integer, MachineState> machineStates = new HashMap<>();
        for (int i = 0; i < JobShopData.numMachines; i++) {
            machineStates.put(i, new MachineState());
        }

        // 处理每个调度步骤
        for (String jobIdStr : scheduleOrder) {
            int jobId = Integer.parseInt(jobIdStr);
            JobState jobState = jobStates.get(jobId);
            Operation op = jobState.getCurrentOperation();

            // 计算开始时间（取机器可用时间和作业前序完成时间的最大值）
            int startTime = Math.max(
                    machineStates.get(op.machineId).availableTime,
                    jobState.getPreviousEndTime()
            );

            // 更新结束时间
            int endTime = startTime + op.processingTime;

            // 更新机器状态
            machineStates.get(op.machineId).availableTime = endTime;

            // 更新作业状态
            jobState.recordOperationTime(endTime);

            // 更新Operation的startTime和endTime
            op.startTime = startTime;
            op.endTime = endTime;
        }

        // 返回更新后的jobs
        return jobs;
    }

    // 辅助类：作业状态跟踪
    private static class JobState {
        private final Job job;
        private int currentOpIndex = 0;
        private int previousEndTime = 0;

        public JobState(Job job) {
            this.job = job;
        }

        public Operation getCurrentOperation() {
            return job.operations.get(currentOpIndex);
        }

        public int getPreviousEndTime() {
            return previousEndTime;
        }

        public void recordOperationTime(int endTime) {
            previousEndTime = endTime;
            currentOpIndex++;
        }
    }

    // 辅助类：机器状态跟踪
    private static class MachineState {
        public int availableTime = 0;
    }

    private static void printScheduleOrder(Algorithm scheduler) {
        System.out.println("Schedule Order: " + scheduler.getScheduleOrder());
    }
    // 新增适配 int[] 的方法
    public static int calculateMakespan(int[] scheduleArray) {
        List<String> converted = Arrays.stream(scheduleArray)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
        try {
            return ScheduleDecoder.calculateMakespan(converted, "input.txt");
        } catch (IOException e) {
            throw new RuntimeException("计算失败", e);
        }
    }

    public static void Crossover() {
        String filePath = "input.txt";
        try {
            JobShopData data = readJobShopFile(filePath);
            List<Algorithm> schedulers = new ArrayList<>();

            // 创建五种算法的实例
            for (int i = 0; i < 5; i++) {
                // 深拷贝作业
                List<Job> copiedJobs = new ArrayList<>();
                for (Job original : data.jobs) {
                    copiedJobs.add(original.deepCopy()); // 使用深拷贝
                }

                // 创建新机器（不需要深拷贝）
                List<Machine> newMachines = new ArrayList<>();
                for (int j = 0; j < JobShopData.numMachines; j++) {
                    newMachines.add(new Machine(j)); // 每次都新建机器
                }

                schedulers.add(new Algorithm(copiedJobs, newMachines));
            }

            // 执行并输出各算法结果
            int[] results = new int[5];
            for (int i = 0; i < 5; i++) {
                Algorithm scheduler = schedulers.get(i);
                scheduler.resetState(); // 执行前重置状态
                switch (i) {
                    case 0: results[0] = scheduler.LPT(); break;
                    case 1: results[1] = scheduler.SPT(); break;
                    case 2: results[2] = scheduler.SRTF(); break;
                    case 3: results[3] = scheduler.LRTF(); break;
                    case 4: results[4] = scheduler.KK(); break;
                }
            }

            System.out.println("=== 基础算法结果 ===");
            System.out.println("LPT Makespan: " + results[0]);
            System.out.println("SPT Makespan: " + results[1]);
            System.out.println("SRTF Makespan: " + results[2]);
            System.out.println("LRTF Makespan: " + results[3]);
            System.out.println("KK Makespan: " + results[4]);

            // 交叉验证部分
            System.out.println("\n=== 交叉验证结果 ===");
            List<int[]> orders = schedulers.stream()
                    .map(s -> toarray(s.getScheduleOrder()))
                    .collect(Collectors.toList());

            int totalCombinations = 0;
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    if (i == j) continue;

                    System.out.println("\n交叉组合 " + getAlgoName(i) + " & " + getAlgoName(j));

                    List<String> childOrder = generateOrder(
                            orders.get(i),
                            orders.get(j),
                            JobShopData.numJobs,
                            JobShopData.numMachines
                    );

                    try {
                        int makespan = ScheduleDecoder.calculateMakespan(childOrder, "input.txt");
                        System.out.println("生成解 makespan: " + makespan);
                        System.out.println("调度顺序: " + String.join(" ", childOrder));
                    } catch (IOException e) {
                        System.err.println("计算异常: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static String getAlgoName(int index) {
        return switch (index) {
            case 0 -> "LPT";
            case 1 -> "SPT";
            case 2 -> "SRTF";
            case 3 -> "LRTF";
            case 4 -> "KK";
            default -> "Unknown";
        };
    }

    public static int[] toarray(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new int[0];
        }
        return Arrays.stream(input.split(" "))
                .mapToInt(Integer::parseInt)
                .toArray();
    }


    public static List<String> generateOrder(int[] parentA, int[] parentB,
                                             int jobCount, int machineCount) {
        // 为每个作业维护工序队列
        Map<Integer, Queue<Integer>> jobQueues = new HashMap<>();
        for (int jobId = 0; jobId < jobCount; jobId++) {
            jobQueues.put(jobId, new LinkedList<>());
            for (int op = 0; op < machineCount; op++) {
                jobQueues.get(jobId).add(jobId); // 每个工序用jobId表示
            }
        }

        List<String> newOrder = new ArrayList<>();
        Random rand = new Random();

        // 创建两个父代的迭代器
        Iterator<Integer> iterA = Arrays.stream(parentA).iterator();
        Iterator<Integer> iterB = Arrays.stream(parentB).iterator();

        while (newOrder.size() < jobCount * machineCount) {
            Iterator<Integer> currentIter = rand.nextBoolean() ? iterA : iterB;

            if (currentIter.hasNext()) {
                int jobId = currentIter.next();
                Queue<Integer> queue = jobQueues.get(jobId);

                if (!queue.isEmpty()) {
                    newOrder.add(String.valueOf(queue.poll()));
                }
            }

            // 处理未完成的作业
            for (Queue<Integer> q : jobQueues.values()) {
                if (!q.isEmpty() && rand.nextDouble() < 0.2) {
                    newOrder.add(String.valueOf(q.poll()));
                }
            }
        }

        return newOrder;
    }
    // 辅助方法：构建orderB的工序队列
    private static Map<Integer, Queue<Integer>> buildOrderBQueues(int[] order, int jobCount, int machineCount) {
        Map<Integer, Queue<Integer>> queues = new HashMap<>();
        Map<Integer, Integer> stepCounters = new HashMap<>();

        // 初始化队列结构
        for (int i = 0; i < jobCount; i++) {
            queues.put(i, new LinkedList<>());
            stepCounters.put(i, 0);
        }

        // 按orderB顺序填充队列（保证工序顺序）
        for (int jobId : order) {
            int step = stepCounters.get(jobId);
            if (step < machineCount) {
                queues.get(jobId).add(jobId);
                stepCounters.put(jobId, step + 1);
            }
        }
        return queues;
    }
    // 关键路径分析方法（示例实现）
    private static List<Integer> findCriticalPath(int[] order, int jobCount) {
        Map<Integer, Integer> lastOccurrence = new HashMap<>();
        for (int i = 0; i < order.length; i++) {
            lastOccurrence.put(order[i], i);
        }
        return new ArrayList<>(lastOccurrence.values());
    }
    public static void initialize()
    {
        String filePath = "input.txt"; // Replace with your input file path
        try {
            // Read the data from the file
            JobShopData data = readJobShopFile(filePath);

            // Create machines
            List<Machine> machines = new ArrayList<>();
            List<Machine> machines1 = new ArrayList<>();

            for (int i = 0; i < JobShopData.numMachines; i++) {
                machines.add(new Machine(i));
            }
            for (int i = 0; i < JobShopData.numMachines; i++) {
                machines1.add(new Machine(i));
            }


            // Create scheduler and process jobs
            Algorithm scheduler = new Algorithm(data.jobs, machines);

            // Calculate Makespan using KK Algorithm
            int makespan = scheduler.LPT();
            printScheduleOrder(scheduler);
            // Output the Makespan
            System.out.println("Makespan: " + makespan);

            // Create Gantt Chart
            GanttChartPrinter ganttChart = new GanttChartPrinter(data.jobs, JobShopScheduling.JobShopData.numMachines,makespan);

            // Add Gantt Chart to a JScrollPane
            JScrollPane scrollPane = new JScrollPane(ganttChart);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            // Create JFrame and add JScrollPane
            JFrame frame = new JFrame("Gantt Chart");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.add(scrollPane);
            frame.setVisible(true);

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    // Helper class to store job shop data
    static class JobShopData {
        static int numJobs;
        static int numMachines;
        List<Job> jobs;

        public JobShopData(int numJobs, int numMachines, List<Job> jobs) {
            JobShopData.numJobs = numJobs;
            JobShopData.numMachines = numMachines;
            this.jobs = jobs;
        }
    }

    public static JobShopData readJobShopFile(String filePath) throws IOException {
        List<Job> jobs = new ArrayList<>();
        int numJobs = 0;
        int numMachines = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            // Skip empty lines or comments
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines or comments
                }

                // Read job and machine info
                String[] jobMachineInfo = line.split("\\s+");
                if (jobMachineInfo.length == 2) {
                    numJobs = Integer.parseInt(jobMachineInfo[0]);
                    numMachines = Integer.parseInt(jobMachineInfo[1]);
                    break; // Stop after reading job and machine info
                }
            }

            // Read the job operations
            for (int i = 0; i < numJobs; i++) {
                line = br.readLine();
                if (line == null || line.trim().isEmpty()) {
                    throw new IOException("Invalid file format: missing data for job " + i);
                }
                String[] jobData = line.trim().split("\\s+");
                Job job = new Job(i);

                // Parse operations (pairs of machineId and processingTime)
                for (int j = 0; j < jobData.length; j += 2) {
                    if (j + 1 >= jobData.length) {
                        throw new IOException("Invalid operation pair for job " + i);
                    }
                    try {
                        int machineId = Integer.parseInt(jobData[j]);
                        int processingTime = Integer.parseInt(jobData[j + 1]);
                        job.addOperation(new Operation(i, j / 2, machineId, processingTime));
                    } catch (NumberFormatException e) {
                        throw new IOException("Invalid number format in operations for job " + i + " at position " + j, e);
                    }
                }

                jobs.add(job);
            }
        }

        return new JobShopData(numJobs, numMachines, jobs);
    }
}
class ScheduleDecoder {
    // 从文件读取Schedule Order
    public static int decodeFromFile(String filePath, String inputFilePath) throws IOException {
        List<String> scheduleOrder = Files.readAllLines(Paths.get(filePath)).stream()
                .flatMap(line -> Arrays.stream(line.split(" ")))
                .collect(Collectors.toList());
        return calculateMakespan(scheduleOrder, inputFilePath);

    }

    // 直接传入Schedule Order数组
    public static int decodeFromArray(List<String> scheduleOrder, String inputFilePath) throws IOException {
        return calculateMakespan(scheduleOrder, inputFilePath);
    }

    public static int calculateMakespan(List<String> scheduleOrder, String inputFilePath) throws IOException {
        // 读取原始作业数据

        JobShopScheduling.JobShopData data = JobShopScheduling.readJobShopFile(inputFilePath);

        // 初始化作业状态
        Map<Integer, JobState> jobStates = new HashMap<>();
        for (Job job : data.jobs) {
            jobStates.put(job.jobId, new JobState(job));
        }

        // 初始化机器状态
        Map<Integer, MachineState> machineStates = new HashMap<>();
        for (int i = 0; i < data.numMachines; i++) {
            machineStates.put(i, new MachineState());
        }

        int makespan = 0;

        // 处理每个调度步骤
        for (String jobIdStr : scheduleOrder) {
            int jobId = Integer.parseInt(jobIdStr);
            JobState jobState = jobStates.get(jobId);
            Operation op = jobState.getCurrentOperation();

            // 计算开始时间（取机器可用时间和作业前序完成时间的最大值）
            int startTime = Math.max(
                    machineStates.get(op.machineId).availableTime,
                    jobState.getPreviousEndTime()
            );

            // 更新结束时间
            int endTime = startTime + op.processingTime;

            // 更新机器状态
            machineStates.get(op.machineId).availableTime = endTime;

            // 更新作业状态
            jobState.recordOperationTime(endTime);

            // 更新全局makespan
            makespan = Math.max(makespan, endTime);
        }

        return makespan;
    }

    // 辅助类：作业状态跟踪
    private static class JobState {
        private final Job job;
        private int currentOpIndex = 0;
        private int previousEndTime = 0;

        public JobState(Job job) {
            this.job = job;
        }

        public Operation getCurrentOperation() {
            return job.operations.get(currentOpIndex);
        }

        public int getPreviousEndTime() {
            return previousEndTime;
        }

        public void recordOperationTime(int endTime) {
            previousEndTime = endTime;
            currentOpIndex++;
        }
    }

    // 辅助类：机器状态跟踪
    private static class MachineState {
        public int availableTime = 0;
    }
}
