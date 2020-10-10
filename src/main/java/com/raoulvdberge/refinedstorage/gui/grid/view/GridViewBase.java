package com.raoulvdberge.refinedstorage.gui.grid.view;

import com.raoulvdberge.refinedstorage.api.network.grid.IGrid;
import com.raoulvdberge.refinedstorage.gui.grid.GuiGrid;
import com.raoulvdberge.refinedstorage.gui.grid.filtering.GridFilterParser;
import com.raoulvdberge.refinedstorage.gui.grid.sorting.GridSorterDirection;
import com.raoulvdberge.refinedstorage.gui.grid.sorting.IGridSorter;
import com.raoulvdberge.refinedstorage.gui.grid.stack.IGridStack;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class GridViewBase implements IGridView {
    private GuiGrid gui;
    private boolean canCraft;

    private IGridSorter defaultSorter;
    private List<IGridSorter> sorters;

    private List<IGridStack> stacks = new ArrayList<>();
    protected Map<Integer, IGridStack> map = new HashMap<>();

    public GridViewBase(GuiGrid gui, IGridSorter defaultSorter, List<IGridSorter> sorters) {
        this.gui = gui;
        this.defaultSorter = defaultSorter;
        this.sorters = sorters;
    }

    @Override
    public List<IGridStack> getStacks() {
        return stacks;
    }

    @Override
    public void sort() {
        if (gui.getGrid().isActive()) {
            this.stacks = map.values().stream()
                    .filter(getActiveFilters())
                    .sorted(getActiveSort())
                    .collect(Collectors.toList());
        } else {
            this.stacks = Collections.emptyList();
        }

        this.gui.updateScrollbar();
    }

    private Predicate<IGridStack> getActiveFilters() {
        IGrid grid = gui.getGrid();
        return GridFilterParser.getFilters(
                grid,
                gui.getSearchField() != null ? gui.getSearchField().getText() : "",
                (grid.getTabSelected() >= 0 && grid.getTabSelected() < grid.getTabs().size()) ? grid.getTabs().get(grid.getTabSelected()).getFilters() : grid.getFilters()
        );
    }

    private Comparator<IGridStack> getActiveSort() {
        IGrid grid = gui.getGrid();
        GridSorterDirection sortingDirection = grid.getSortingDirection() == IGrid.SORTING_DIRECTION_DESCENDING ? GridSorterDirection.DESCENDING : GridSorterDirection.ASCENDING;

        return Stream.concat(Stream.of(defaultSorter), sorters.stream().filter(s -> s.isApplicable(grid)))
                .map(sorter -> (Comparator<IGridStack>) (o1, o2) -> sorter.compare(o1, o2, sortingDirection))
                .reduce((l, r) -> r.thenComparing(l))
                .orElseThrow(IllegalStateException::new);
    }

    @Override
    public void setCanCraft(boolean canCraft) {
        this.canCraft = canCraft;
    }

    @Override
    public boolean canCraft() {
        return canCraft;
    }
}
