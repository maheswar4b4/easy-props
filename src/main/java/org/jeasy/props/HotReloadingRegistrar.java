/**
 * The MIT License
 *
 *   Copyright (c) 2017, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 */
package org.jeasy.props;

import org.jeasy.props.annotations.HotReload;
import org.jeasy.props.api.PropertiesInjector;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.jeasy.props.DaemonThreadFactory.newDaemonThreadFactory;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * Component responsible for registering hot reloading tasks for a given object.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
class HotReloadingRegistrar {

    private Map<Object, Runnable> hotReloadingTasks = new HashMap<>();
    private ScheduledExecutorService scheduledExecutorService = newSingleThreadScheduledExecutor(newDaemonThreadFactory());

    void registerHotReloadingTask(final PropertiesInjector propertiesInjector, final Object target) {
        if (shouldBeHotReloaded(target)) {
            HotReload hotReload = target.getClass().getAnnotation(HotReload.class);
            long period = hotReload.period();
            TimeUnit unit = hotReload.unit();
            PropertiesInjectionTask propertiesInjectionTask = new PropertiesInjectionTask(propertiesInjector, target);
            scheduledExecutorService.scheduleAtFixedRate(propertiesInjectionTask, 0, period, unit);
            hotReloadingTasks.put(target, propertiesInjectionTask);
        }
    }

    private boolean shouldBeHotReloaded(final Object target) {
        return target.getClass().isAnnotationPresent(HotReload.class) && !hotReloadingTasks.containsKey(target);
    }

}
