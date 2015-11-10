/*
 * Copyright (c) 2015 IBM Corporation and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brunel.maps;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A set of files that could potentially cover the mapping we need
 */
class GeoFileGroup {
    private static final int MAX_FILES = 3;                 // No more files than this
    public final Set<GeoFile> files;
    private final int requiredFeatureCount;
    private final Set<Object> featureSet;
    private Rect totalBounds;

    public GeoFileGroup(int requiredFeatureCount, Collection<GeoFile> files, Collection<?> features) {
        this.requiredFeatureCount = requiredFeatureCount;
        this.files = new LinkedHashSet<GeoFile>(files);
        this.featureSet = new HashSet<Object>(features);
    }

    public GeoFileGroup add(GeoFile file, List<?> features) {
        if (files.contains(file)) return null;                      // Already included
        Set<Object> combinedFeatures = new HashSet<Object>(featureSet);
        if (!combinedFeatures.addAll(features)) return null;        // if not change, don't use this
        Set<GeoFile> combinedFiles = new HashSet<GeoFile>(files);
        combinedFiles.add(file);
        return new GeoFileGroup(requiredFeatureCount, combinedFiles, combinedFeatures);
    }

    public boolean cannotImprove(GeoFileGroup best, int maxFeaturesPerFile) {
        if (featureSet.size() == requiredFeatureCount) return true;         // Nothing new can get added
        if (files.size() == MAX_FILES) return true;                         // Limited number of files

        // An upper bound on the number of features we could add
        int upperFeatureBound = featureSet.size() + (MAX_FILES - files.size()) * maxFeaturesPerFile;
        return upperFeatureBound < best.featureSet.size();
    }

    public boolean isBetter(GeoFileGroup o) {
        if (o == this) return false;

        // More features are better
        int d = featureSet.size() - o.featureSet.size();
        if (d < 0) return false;
        if (d > 0) return true;

        double myScore = area() * (1 + files.size());
        double otherScore = o.area() * (1 + o.files.size());

        return myScore < otherScore;
    }

    private double area() {
        if (files.isEmpty()) return 0;
        if (totalBounds == null) {
            for (GeoFile f : files) totalBounds = f.bounds.union(totalBounds);
        }
        return totalBounds.area();
    }

    public String toString() {
        return files + ":" + featureSet.size() + "/" + requiredFeatureCount;
    }
}
