public class CustomStatusManager {
    private static CustomStatusManager manager;
    private final BiMap<Integer, CustomStatus> allStatuses;
    private Long tickCount = 0L;

    //I am basically ripping custom enchant manager because uh... yes. if it works there, it should work here?

    private CustomStatusManager() {
        new BukkitRunnable() {
            @Override
            public void run() {
                tickCount++;
            }
        }.runTaskTimer(EnchantsSquared.getInstance(), 0L, 1L);

        registerStatuses();
    }

    public Collection<CustomStatus> getStatusesMatchingFilter(Predicate<CustomStatus> filter){
        return allStatuses.values().stream().filter(filter).distinct()
                .sorted(Comparator.comparingInt(c -> c.getPriority().getPriority())).collect(Collectors.toList());
    }

    private void registerStatuses() {
        registerStatus(new voidTouchedStatus(1, "void_touched"));
    }

    private void registerStatus(CustomStatus status) {
        allStatuses.put(status.getId(), status);
    }

    public Long getTickCount() {
        return this.tickCount;
    }

    public CustomStatus getStatusFromType(String status) {
        for (CustomStatus s : allStatuses.values()){
            if (s.getType().equals(status)){
                return s;
            }
        }
        return null;
    }

    public static CustomStatusManager getInstance(){
        if (manager == null) manager = new CustomStatusManager();
        return manager;
    }
}